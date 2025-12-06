package kr.hhplus.be.server.presentation.web.coupon

import com.jayway.jsonpath.JsonPath
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kr.hhplus.be.server.common.annotation.IntegrationTest
import kr.hhplus.be.server.infrastructure.persistence.coupon.CouponIssuanceJpaRepository
import kr.hhplus.be.server.infrastructure.persistence.coupon.CouponJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.time.LocalDateTime

@IntegrationTest
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SqlGroup(
    Sql(
        scripts = ["/sql/coupon-issuance-concurrency-setup.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    ),
    Sql(
        scripts = ["/sql/coupon-issuance-concurrency-cleanup.sql"],
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    ),
)
class CouponIssuanceConcurrencyIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var couponJpaRepository: CouponJpaRepository

    @Autowired
    private lateinit var couponIssuanceJpaRepository: CouponIssuanceJpaRepository

    private val fixedNow = LocalDateTime.of(2025, 9, 24, 0, 0, 0)

    @BeforeEach
    fun setUp() {
        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns fixedNow
    }

    @AfterEach
    fun tearDown() {
        io.mockk.unmockkStatic(LocalDateTime::class)
    }

    @Test
    @DisplayName("동시에 쿠폰 발급 요청 시 한 명만 성공한다")
    fun issueCoupon_concurrency_allowOnlySingleSuccess() {
        runBlocking {
            val startSignal = CompletableDeferred<Unit>()
            val scenarios =
                listOf(
                    ConcurrencyRequest(memberId = 101L, idempotencyKey = "coroutine-101"),
                    ConcurrencyRequest(memberId = 102L, idempotencyKey = "coroutine-102"),
                )

            val jobs =
                scenarios.map { scenario ->
                    async(Dispatchers.IO) {
                        startSignal.await()
                        val mvcResult =
                            mockMvc
                                .perform(
                                    MockMvcRequestBuilders
                                        .post("/api/v1/coupons/issuance")
                                        .header("X-Member-Id", scenario.memberId)
                                        .header("Idempotency-Key", scenario.idempotencyKey)
                                        .content("""{"couponSummaryId": 100}""")
                                        .contentType(MediaType.APPLICATION_JSON),
                                ).andReturn()

                        mvcResult.response
                    }
                }

            startSignal.complete(Unit)
            val responses = jobs.awaitAll()

            responses
                .partition { it.status == 200 }
                .also { (success, failure) ->
                    assertAll(
                        { assertThat(success).describedAs("성공 응답").hasSize(1) },
                        { assertThat(failure).describedAs("실패 응답").hasSize(1) },
                        { assertThat(failure[0].status).isEqualTo(409) },
                        {
                            val message = JsonPath.read<String>(failure[0].contentAsString, "$.message")
                            assertThat(message).contains("쿠폰발급수량이 부족합니다.")
                        },
                    )
                }

            val issuedCoupons =
                withContext(Dispatchers.IO) {
                    couponJpaRepository
                        .findAll()
                        .filter { it.couponSummaryJpaEntity.id == 100L }
                }
            assertThat(issuedCoupons).describedAs("DB에는 한 건의 쿠폰만 발급되어야 한다").hasSize(1)

            val issuance =
                withContext(Dispatchers.IO) {
                    couponIssuanceJpaRepository
                        .findById(100L)
                        .orElseThrow { IllegalStateException("쿠폰 발급 정보를 찾을 수 없습니다.") }
                }

            assertThat(issuance.issuedCount).describedAs("발급 수량이 1로 업데이트되어야 한다").isEqualTo(1)
        }
    }

    private data class ConcurrencyRequest(
        val memberId: Long,
        val idempotencyKey: String,
    )
}
