package kr.hhplus.be.server.domain.utils

import kr.hhplus.be.server.domain.model.balance.MemberBalance
import kr.hhplus.be.server.domain.model.coupon.Coupon
import kr.hhplus.be.server.domain.model.coupon.CouponIssuance
import kr.hhplus.be.server.domain.model.coupon.CouponSummary
import kr.hhplus.be.server.domain.model.coupon.policy.CouponIssuancePolicy
import kr.hhplus.be.server.domain.model.member.Member
import kr.hhplus.be.server.domain.model.order.OrderItem
import kr.hhplus.be.server.domain.model.order.OrderSummary
import kr.hhplus.be.server.domain.model.product.ProductSummary
import java.time.LocalDateTime

object StubFactory {
    // ---------------- Coupon ----------------
    fun couponSummary(
        id: Long? = 1L,
        name: String = "테스트쿠폰",
        discountPercentage: Long = 10L,
        validDays: Int? = 30,
    ): CouponSummary =
        CouponSummary(
            id = id,
            name = name,
            discountPercentage = discountPercentage,
            validDays = validDays,
        )

    fun coupon(
        id: Long? = 1L,
        couponSummary: CouponSummary = couponSummary(),
        memberId: Long = 1L,
        usingAt: LocalDateTime? = null,
        now: LocalDateTime = LocalDateTime.now(),
        expiredAt: LocalDateTime? = null,
    ): Coupon =
        Coupon(
            id = id,
            memberId = memberId,
            couponSummary = couponSummary,
            usingAt = usingAt,
            expiredAt = expiredAt ?: couponSummary.calculateExpiredAt(now),
        )

    // CouponIssuance, CouponIssuancePolicy 등은 필요시 추가 구현

    // ---------------- Member ----------------
    fun member(
        id: Long? = 1L,
        email: String = "user@test.com",
        pwd: String = "password",
    ): Member = Member(id = id, email = email, pwd = pwd)

    // ---------------- MemberBalance ----------------
    fun memberBalance(
        id: Long? = 1L,
        balance: Long = 10_000L,
        member: Member = member(),
    ): MemberBalance = MemberBalance(id = id, balance = balance, member = member)

    // ---------------- Product ----------------
    fun productSummary(
        id: Long? = 1L,
        name: String = "상품명",
        price: Int = 1000,
        stockQuantity: Int = 100,
    ): ProductSummary = ProductSummary(id = id, name = name, price = price, stockQuantity = stockQuantity)

    // ---------------- Order ----------------
    fun orderItem(
        id: Long? = 1L,
        orderSummaryId: Long = 0L,
        productSummaryId: Long = 1L,
        quantity: Int = 1,
        price: Int = 1000,
    ): OrderItem =
        OrderItem(
            id = id,
            orderSummaryId = orderSummaryId,
            productSummaryId = productSummaryId,
            quantity = quantity,
            price = price,
        )

    fun orderSummary(
        id: Long? = 1L,
        memberId: Long = 1L,
        orderItems: List<OrderItem> = emptyList(),
    ): OrderSummary = OrderSummary(id = id, memberId = memberId, orderItems = orderItems)
}
