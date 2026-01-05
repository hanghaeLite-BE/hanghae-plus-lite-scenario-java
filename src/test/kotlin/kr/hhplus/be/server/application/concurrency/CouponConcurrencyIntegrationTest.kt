package kr.hhplus.be.server.application.concurrency

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kr.hhplus.be.server.common.annotation.IntegrationTest
import kr.hhplus.be.server.common.config.NoOpEventPublisherConfig
import kr.hhplus.be.server.common.support.postJsonWithIdempotency
import kr.hhplus.be.server.infrastructure.persistence.coupon.CouponIssuanceJpaRepository
import kr.hhplus.be.server.infrastructure.persistence.coupon.CouponJpaRepository
import kr.hhplus.be.server.infrastructure.persistence.order.OrderSummaryJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.web.servlet.MockMvc

/**
 * 쿠폰 동시성 이슈 검증 테스트
 *
 * 이 테스트는 현재 코드의 동시성 이슈를 검증합니다.
 * - TC-CPN-001: 선착순 쿠폰 수량 초과 발급 검증 (maxCount 제한)
 * - TC-CPN-002: 동일 회원 쿠폰 중복 발급 검증 (OnePerMember 정책)
 * - TC-CPN-003: 동일 쿠폰 동시 사용 검증 (주문 시)
 *
 * 동시성 이슈가 해결되면 이 테스트들이 통과해야 합니다.
 *
 * > ⚠️ 현재 CouponIssuanceJpaRepository에 Pessimistic Lock이 임시로 적용되어 있습니다.
 * > 이 테스트는 Lock이 없다고 가정했을 때의 동시성 이슈를 검증합니다.
 * > 임시 Lock으로 인해 테스트가 통과할 수 있으며, 이는 Lock이 정상 동작함을 의미합니다.
 */
@IntegrationTest
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SqlGroup(
    Sql(
        scripts = ["/sql/coupon-concurrency-setup.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    ),
    Sql(
        scripts = ["/sql/coupon-concurrency-cleanup.sql"],
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    ),
)
@Import(NoOpEventPublisherConfig::class)
class CouponConcurrencyIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var couponIssuanceJpaRepository: CouponIssuanceJpaRepository

    @Autowired
    private lateinit var couponJpaRepository: CouponJpaRepository

    @Autowired
    private lateinit var orderSummaryJpaRepository: OrderSummaryJpaRepository

    @Nested
    @DisplayName("TC-CPN-001: 쿠폰 발급 API - 선착순 쿠폰 수량 초과 발급 검증")
    inner class ConcurrentCouponIssuanceOverLimitTest {

        /**
         * 검증 대상: CouponIssuanceUseCase의 쿠폰 수량 제한 동시성 처리
         *
         * 시나리오: 2명의 사용자가 동시에 maxCount=1인 선착순 쿠폰 발급 요청
         *
         * 쿠폰 설정:
         * - maxCount: 1 (1명만 발급 가능)
         * - issuedCount: 0 (초기 발급 수)
         *
         * 요청:
         * - 사용자 A: 쿠폰 발급 요청
         * - 사용자 B: 쿠폰 발급 요청 (동시)
         *
         * 예상 결과 (정상): 1명 성공, 1명 실패, issuedCount=1
         * 동시성 이슈 발생 시: 2명 모두 성공, issuedCount=2 (또는 1로 Lost Update)
         *
         * 발생 원인: 두 트랜잭션이 동시에 issuedCount(0)를 읽고,
         *          maxCount(1)와 비교하여 모두 발급 가능으로 판단,
         *          각각 issuedCount를 1로 증가시키면서 초과 발급 발생
         */
        @Test
        @DisplayName("선착순 쿠폰(maxCount=1)에 2명 동시 요청 시 1명만 발급되어야 한다")
        fun concurrentCouponIssuance_shouldAllowOnlyOneSuccess_whenMaxCountIsOne() {
            val couponSummaryId = 6001L
            val couponIssuanceId = 6001L
            val maxCount = 1

            runBlocking {
                val startSignal = CompletableDeferred<Unit>()

                val requests = listOf(
                    CouponIssuanceRequest(
                        memberId = 5001L,
                        idempotencyKey = "coupon-issue-user-a",
                        couponSummaryId = couponSummaryId,
                    ),
                    CouponIssuanceRequest(
                        memberId = 5002L,
                        idempotencyKey = "coupon-issue-user-b",
                        couponSummaryId = couponSummaryId,
                    ),
                )

                val jobs = requests.map { request ->
                    async(Dispatchers.IO) {
                        startSignal.await()
                        mockMvc
                            .perform(
                                postJsonWithIdempotency(
                                    uri = "/api/v1/coupons/issuance",
                                    body = request.toJson(),
                                    memberId = request.memberId,
                                    idempotencyKey = request.idempotencyKey,
                                ),
                            )
                            .andReturn()
                            .response
                    }
                }

                startSignal.complete(Unit)
                val responses = jobs.awaitAll()

                // 응답 분석
                val successResponses = responses.filter { it.status == 200 }
                val failureResponses = responses.filter { it.status != 200 }

                // DB에서 최종 발급 수 확인
                val finalIssuedCount = withContext(Dispatchers.IO) {
                    couponIssuanceJpaRepository
                        .findById(couponIssuanceId)
                        .orElseThrow { IllegalStateException("쿠폰 발급 정보를 찾을 수 없습니다.") }
                        .issuedCount
                }

                // 실제 발급된 쿠폰 수 확인
                val issuedCoupons = withContext(Dispatchers.IO) {
                    couponJpaRepository.findAll()
                        .filter { it.couponSummaryJpaEntity.id == couponSummaryId }
                }

                assertAll(
                    {
                        assertThat(successResponses.size)
                            .describedAs("1명만 쿠폰 발급 성공해야 함 (maxCount 초과 발급 시 실패)")
                            .isEqualTo(1)
                    },
                    {
                        assertThat(failureResponses.size)
                            .describedAs("1명은 실패해야 함 (수량 소진)")
                            .isEqualTo(1)
                    },
                    {
                        assertThat(finalIssuedCount)
                            .describedAs("발급 수는 maxCount($maxCount)를 초과하면 안됨")
                            .isLessThanOrEqualTo(maxCount)
                    },
                    {
                        assertThat(issuedCoupons.size)
                            .describedAs("실제 발급된 쿠폰은 1개여야 함")
                            .isEqualTo(1)
                    },
                )
            }
        }
    }

    @Nested
    @DisplayName("TC-CPN-002: 쿠폰 발급 API - 동일 회원 쿠폰 중복 발급 검증 (OnePerMember 정책)")
    inner class ConcurrentCouponIssuanceDuplicateMemberTest {

        /**
         * 검증 대상: OnePerMemberPolicy의 동시성 처리
         *
         * 시나리오: 동일 사용자가 동시에 2번의 쿠폰 발급 요청 (OnePerMember 정책 적용 쿠폰)
         *
         * 쿠폰 설정:
         * - maxCount: 100 (충분한 수량)
         * - OnePerMember 정책 적용 (회원당 1회 발급 제한)
         *
         * 요청:
         * - 사용자 C: 쿠폰 발급 요청 (idempotency-key-1)
         * - 사용자 C: 쿠폰 발급 요청 (idempotency-key-2) - 동시
         *
         * 예상 결과 (정상): 1건 성공, 1건 실패, 쿠폰 1개만 발급
         * 동시성 이슈 발생 시: 2건 모두 성공, 쿠폰 2개 발급 (중복 발급)
         *
         * 발생 원인: 두 트랜잭션이 동시에 "해당 회원에게 발급된 쿠폰 없음"을 확인,
         *          각각 쿠폰을 발급하면서 중복 발급 발생
         *
         * NOTE: OnePerMember 정책 검증은 회원별 기발급 여부를 체크하는 로직의 동시성을 검증합니다.
         */
        @Test
        @DisplayName("OnePerMember 정책 쿠폰에 동일 회원이 동시 요청 시 1건만 발급되어야 한다")
        fun concurrentCouponIssuance_sameMember_shouldAllowOnlyOneSuccess() {
            val memberId = 5003L
            val couponSummaryId = 6002L
            val couponIssuanceId = 6002L

            runBlocking {
                val startSignal = CompletableDeferred<Unit>()

                // 동일 사용자가 서로 다른 idempotency key로 2건의 쿠폰 발급 요청
                val requests = listOf(
                    CouponIssuanceRequest(
                        memberId = memberId,
                        idempotencyKey = "coupon-duplicate-1",
                        couponSummaryId = couponSummaryId,
                    ),
                    CouponIssuanceRequest(
                        memberId = memberId,
                        idempotencyKey = "coupon-duplicate-2",
                        couponSummaryId = couponSummaryId,
                    ),
                )

                val jobs = requests.map { request ->
                    async(Dispatchers.IO) {
                        startSignal.await()
                        mockMvc
                            .perform(
                                postJsonWithIdempotency(
                                    uri = "/api/v1/coupons/issuance",
                                    body = request.toJson(),
                                    memberId = request.memberId,
                                    idempotencyKey = request.idempotencyKey,
                                ),
                            )
                            .andReturn()
                            .response
                    }
                }

                startSignal.complete(Unit)
                val responses = jobs.awaitAll()

                // 응답 분석
                val successResponses = responses.filter { it.status == 200 }
                val failureResponses = responses.filter { it.status != 200 }

                // DB에서 발급 수 확인
                val finalIssuedCount = withContext(Dispatchers.IO) {
                    couponIssuanceJpaRepository
                        .findById(couponIssuanceId)
                        .orElseThrow { IllegalStateException("쿠폰 발급 정보를 찾을 수 없습니다.") }
                        .issuedCount
                }

                // 해당 회원에게 발급된 쿠폰 수 확인
                val memberCoupons = withContext(Dispatchers.IO) {
                    couponJpaRepository.findAllByMemberId(memberId)
                        .filter { it.couponSummaryJpaEntity.id == couponSummaryId }
                }

                assertAll(
                    {
                        assertThat(successResponses.size)
                            .describedAs("1건만 쿠폰 발급 성공해야 함 (OnePerMember 정책)")
                            .isEqualTo(1)
                    },
                    {
                        assertThat(failureResponses.size)
                            .describedAs("1건은 실패해야 함 (이미 발급됨)")
                            .isEqualTo(1)
                    },
                    {
                        assertThat(finalIssuedCount)
                            .describedAs("발급 수는 1이어야 함")
                            .isEqualTo(1)
                    },
                    {
                        assertThat(memberCoupons.size)
                            .describedAs("해당 회원에게 발급된 쿠폰은 1개여야 함 (중복 발급 방지)")
                            .isEqualTo(1)
                    },
                )
            }
        }
    }

    @Nested
    @DisplayName("TC-CPN-003: 주문 API - 동일 쿠폰 동시 사용 검증")
    inner class ConcurrentCouponUsageTest {

        /**
         * 검증 대상: MyCouponUseCase.using() 메서드의 동시성 처리
         *
         * 시나리오: 같은 회원이 동일한 쿠폰으로 동시에 2건의 주문 요청
         *
         * 쿠폰 상태:
         * - 회원에게 발급된 쿠폰 1개 (미사용)
         * - usingAt: NULL
         *
         * 요청:
         * - 주문1: 쿠폰 적용하여 주문
         * - 주문2: 동일 쿠폰으로 주문 (동시)
         *
         * 예상 결과 (정상): 1건 성공, 1건 실패 (이미 사용된 쿠폰)
         * 동시성 이슈 발생 시: 2건 모두 성공, 쿠폰 중복 사용 (할인 금액 손실)
         *
         * 발생 원인: 두 트랜잭션이 동시에 usingAt(NULL)을 읽고,
         *          각각 쿠폰 미사용으로 판단하여 사용 처리 진행
         *
         * NOTE: 현재는 Member Lock으로 간접 보호되어 있으나,
         *       Lock 없이도 usingAt 체크가 정확히 동작해야 함
         */
        @Test
        @DisplayName("동일 쿠폰으로 동시 주문 시 1건만 쿠폰 적용되어야 한다")
        fun concurrentOrderWithSameCoupon_shouldAllowOnlyOneCouponUsage() {
            val memberId = 5004L
            val couponSummaryId = 6003L
            val couponId = 6003L
            val productId = 6001L
            val productPrice = 10000

            runBlocking {
                val startSignal = CompletableDeferred<Unit>()

                // 동일 사용자가 동일 쿠폰으로 2건의 주문 요청
                val requests = listOf(
                    OrderWithCouponRequest(
                        memberId = memberId,
                        idempotencyKey = "coupon-use-order-1",
                        couponSummaryId = couponSummaryId,
                        productId = productId,
                        quantity = 1,
                        price = productPrice,
                    ),
                    OrderWithCouponRequest(
                        memberId = memberId,
                        idempotencyKey = "coupon-use-order-2",
                        couponSummaryId = couponSummaryId,
                        productId = productId,
                        quantity = 1,
                        price = productPrice,
                    ),
                )

                val jobs = requests.map { request ->
                    async(Dispatchers.IO) {
                        startSignal.await()
                        mockMvc
                            .perform(
                                postJsonWithIdempotency(
                                    uri = "/api/v1/orders",
                                    body = request.toJson(),
                                    memberId = request.memberId,
                                    idempotencyKey = request.idempotencyKey,
                                ),
                            )
                            .andReturn()
                            .response
                    }
                }

                startSignal.complete(Unit)
                val responses = jobs.awaitAll()

                // 응답 분석
                val successResponses = responses.filter { it.status == 200 }
                val failureResponses = responses.filter { it.status != 200 }

                // DB에서 쿠폰 사용 상태 확인
                val coupon = withContext(Dispatchers.IO) {
                    couponJpaRepository.findById(couponId)
                        .orElseThrow { IllegalStateException("쿠폰을 찾을 수 없습니다.") }
                }

                // 생성된 주문 수 확인
                val createdOrders = withContext(Dispatchers.IO) {
                    orderSummaryJpaRepository.findAll()
                        .filter { it.memberId == memberId }
                }

                assertAll(
                    {
                        assertThat(successResponses.size)
                            .describedAs("1건만 주문 성공해야 함 (쿠폰 중복 사용 방지)")
                            .isEqualTo(1)
                    },
                    {
                        assertThat(failureResponses.size)
                            .describedAs("1건은 실패해야 함 (이미 사용된 쿠폰)")
                            .isEqualTo(1)
                    },
                    {
                        assertThat(coupon.usingAt)
                            .describedAs("쿠폰은 사용됨 상태여야 함 (usingAt NOT NULL)")
                            .isNotNull()
                    },
                    {
                        assertThat(createdOrders.size)
                            .describedAs("주문은 1건만 생성되어야 함")
                            .isEqualTo(1)
                    },
                )
            }
        }
    }

    private data class CouponIssuanceRequest(
        val memberId: Long,
        val idempotencyKey: String,
        val couponSummaryId: Long,
    ) {
        fun toJson(): String =
            """
                {
                  "couponSummaryId": $couponSummaryId
                }
            """.trimIndent()
    }

    private data class OrderWithCouponRequest(
        val memberId: Long,
        val idempotencyKey: String,
        val couponSummaryId: Long,
        val productId: Long,
        val quantity: Int,
        val price: Int,
    ) {
        fun toJson(): String {
            val totalAmount = price * quantity
            val discountAmount = totalAmount / 10  // 10% 할인
            val chargeAmount = totalAmount - discountAmount
            return """
                {
                  "couponSummaryId": $couponSummaryId,
                  "orderItems": [
                    {
                      "productSummaryId": $productId,
                      "quantity": $quantity,
                      "price": $price
                    }
                  ],
                  "paymentSummary": {
                    "method": "POINT",
                    "totalAmount": $totalAmount,
                    "discountAmount": $discountAmount,
                    "chargeAmount": $chargeAmount
                  }
                }
            """.trimIndent()
        }
    }
}
