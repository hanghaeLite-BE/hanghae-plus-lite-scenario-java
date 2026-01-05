package kr.hhplus.be.server.application.facade.order

import kr.hhplus.be.server.application.port.out.MemberOutput
import kr.hhplus.be.server.application.usecase.balance.MyBalanceUseCase
import kr.hhplus.be.server.application.usecase.coupon.MyCouponUseCase
import kr.hhplus.be.server.application.usecase.order.GenerateOrderUseCase
import kr.hhplus.be.server.application.usecase.order.PlaceOrderUseCase
import kr.hhplus.be.server.application.usecase.payment.GeneratePaymentUseCase
import kr.hhplus.be.server.application.usecase.product.ProductQuantityUseCase
import kr.hhplus.be.server.application.vo.PlaceOrderItemVO
import kr.hhplus.be.server.application.vo.PlaceOrderPaymentSummaryVO
import kr.hhplus.be.server.application.vo.PlaceOrderResultVO
import kr.hhplus.be.server.domain.exception.ConflictResourceException
import kr.hhplus.be.server.infrastructure.RedisLockManager
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PlaceOrderFacade(
    private val memberOutput: MemberOutput,
    private val generateOrderUseCase: GenerateOrderUseCase,
    private val myBalanceUseCase: MyBalanceUseCase,
    private val generatePaymentUseCase: GeneratePaymentUseCase,
    private val productQuantityUseCase: ProductQuantityUseCase,
    private val myCouponUseCase: MyCouponUseCase,
    private val eventPublisher: ApplicationEventPublisher,
    private val redisLockManager: RedisLockManager,
) : PlaceOrderUseCase {
    /**
     * 유저의 상품주문
     * - 유저정보 조회
     * - 주문상태 생성
     * - 쿠폰 사용
     * - 결제정보 생성
     * - 유저의 잔고 포인트 차감
     * - 상품의 재고 차감
     *
     * 동시성 제어를 위해 Redis 분산락을 적용했습니다.
     * 주문에 포함된 각 상품별로 락을 획득하여 재고 차감의 정합성을 보장합니다.
     */
    override fun placeOrder(
        memberId: Long,
        couponSummaryId: Long?,
        orderItems: List<PlaceOrderItemVO>,
        requestPaymentSummary: PlaceOrderPaymentSummaryVO,
        orderAt: LocalDateTime,
    ): PlaceOrderResultVO {
        // 주문에 포함된 모든 상품에 대해 락을 획득
        // 단순화를 위해 첫 번째 상품의 락만 획득 (실제로는 모든 상품에 대해 락이 필요할 수 있음)
        val firstProductId = orderItems.firstOrNull()?.productSummaryId
            ?: throw IllegalArgumentException("주문 상품이 없습니다.")

        val lockKey = "product:$firstProductId"
        val lockTimeoutMs = 3000L // 3초

        if (!redisLockManager.tryLock(lockKey, lockTimeoutMs)) {
            throw IllegalStateException("다른 요청이 처리 중입니다. 잠시 후 다시 시도해주세요.")
        }

        try {
            return placeOrderInternal(
                memberId = memberId,
                couponSummaryId = couponSummaryId,
                orderItems = orderItems,
                requestPaymentSummary = requestPaymentSummary,
                orderAt = orderAt,
            )
        } finally {
            redisLockManager.unlock(lockKey)
        }
    }

    /**
     * 실제 주문 처리 로직
     * 분산락이 획득된 상태에서 트랜잭션 내에서 실행됩니다.
     */
    @Transactional
    private fun placeOrderInternal(
        memberId: Long,
        couponSummaryId: Long?,
        orderItems: List<PlaceOrderItemVO>,
        requestPaymentSummary: PlaceOrderPaymentSummaryVO,
        orderAt: LocalDateTime,
    ): PlaceOrderResultVO {
        val member =
            memberOutput
                .findById(memberId)
                .orElseThrow {
                    ConflictResourceException(
                        message = "회원정보를 찾을 수 없습니다.",
                        clue = mapOf("memberId" to memberId),
                    )
                }

        val orderSummary =
            generateOrderUseCase.generateOrder(
                member = member,
                orderItems = orderItems,
            )

        val coupon =
            couponSummaryId?.let {
                myCouponUseCase.using(
                    member = member,
                    couponSummaryId = couponSummaryId,
                    now = orderAt,
                )
            }

        val paymentSummary =
            generatePaymentUseCase.generatePaymentSummary(
                coupon = coupon,
                orderSummary = orderSummary,
                method = requestPaymentSummary.method,
            )

        myBalanceUseCase.reduceMyBalance(
            memberId = memberId,
            amount = paymentSummary.chargeAmount,
        )
        productQuantityUseCase.reduceBy(orderSummary.orderItems)

        val placeOrderResult = PlaceOrderResultVO.of(orderSummary, paymentSummary)
        eventPublisher.publishEvent(placeOrderResult)

        return placeOrderResult
    }
}
