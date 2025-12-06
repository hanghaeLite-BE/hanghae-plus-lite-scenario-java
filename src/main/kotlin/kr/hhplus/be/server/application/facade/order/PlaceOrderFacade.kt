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
) : PlaceOrderUseCase {
    /**
     * TODO: 동시성 이슈 방지를 위해 분산락 적용 필요. 지금은 해당 이슈를 고려하지 않음.
     * 유저의 상품주문
     * - 유저정보 조회
     * - 주문상태 생성
     * - 쿠폰 사용
     * - 결제정보 생성
     * - 유저의 잔고 포인트 차감
     * - 상품의 재고 차감
     */
    @Transactional
    override fun placeOrder(
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
                member = member,
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
