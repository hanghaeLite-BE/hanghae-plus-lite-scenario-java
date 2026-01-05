package kr.hhplus.be.server.domain.model.coupon

import kr.hhplus.be.server.domain.exception.ConflictResourceException
import java.time.LocalDateTime

data class Coupon(
    val id: Long?,
    val memberId: Long,
    val couponSummary: CouponSummary,
    val usingAt: LocalDateTime?,
    val expiredAt: LocalDateTime?,
) {
    fun using(now: LocalDateTime = LocalDateTime.now()): Coupon {
        if (usingAt != null) {
            throw ConflictResourceException(
                message = "이미 사용된 쿠폰입니다.",
                clue = mapOf("couponId" to "$id", "usingAt" to "$usingAt"),
            )
        }

        if (isExpired(now)) {
            throw ConflictResourceException(
                message = "만료된 쿠폰입니다.",
                clue = mapOf(
                    "couponId" to "$id",
                    "expiredAt" to "$expiredAt",
                    "now" to "$now"
                )
            )
        }

        return this.copy(usingAt = now)
    }

    /**
     * 쿠폰이 만료되었는지 여부
     * expiredAt이 null인 경우 만료시점이 없으므로 false 반환
     */
    private fun isExpired(now: LocalDateTime = LocalDateTime.now()): Boolean = expiredAt?.let { it <= now } ?: false


    fun calculateDiscountAmount(totalAmount: Long): Long {
        return totalAmount * couponSummary.discountPercentage / 100
    }

    companion object {
        fun create(
            memberId: Long,
            couponSummary: CouponSummary,
            now: LocalDateTime,
        ): Coupon =
            Coupon(
                id = null,
                memberId = memberId,
                couponSummary = couponSummary,
                usingAt = null,
                expiredAt = couponSummary.calculateExpiredAt(now),
            )
    }
}
