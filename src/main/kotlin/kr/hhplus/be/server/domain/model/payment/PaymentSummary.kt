package kr.hhplus.be.server.domain.model.payment

import kr.hhplus.be.server.domain.model.coupon.Coupon
import kr.hhplus.be.server.domain.model.member.Member
import kr.hhplus.be.server.domain.model.order.OrderSummary

data class PaymentSummary(
    val id: Long? = null,
    val method: PaymentMethod,
    val totalAmount: Long,
    val discountAmount: Long,
    val chargeAmount: Long,
    val orderSummaryId: Long,
    val couponId: Long?,
) {
    companion object {
        /**
         * 결제정보 생성
         * - 결제수단 검증
         * - 주문 상품들의 총 금액 계산
         * - 쿠폰 적용 후 결제 금액 계산
         * - 결제수단, 주문정보 포함한 결제정보를 생성
         */
        fun createPaymentSummary(
            method: String,
            coupon: Coupon?,
            orderSummary: OrderSummary,
        ): PaymentSummary {
            val method = PaymentMethod.from(method) ?: throw IllegalArgumentException("지원하지않는 결제수단입니다.")
            val totalAmount = orderSummary.getTotalAmount()
            val discountAmount = coupon?.calculateDiscountAmount(totalAmount) ?: 0L

            return PaymentSummary(
                method = method,
                totalAmount = totalAmount,
                discountAmount = discountAmount,
                chargeAmount = totalAmount - discountAmount,
                orderSummaryId = orderSummary.id!!,
                couponId = coupon?.id,
            )
        }
    }
}

enum class PaymentMethod {
    POINT,
    CARD,
    PHONE,
    BANK_TRANSFER,
    VIRTUAL_ACCOUNT, ;

    companion object {
        fun from(method: String): PaymentMethod? = entries.find { it.name == method }
    }
}
