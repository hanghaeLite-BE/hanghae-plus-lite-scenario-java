package kr.hhplus.be.server.domain.model.coupon

import java.time.LocalDateTime

data class CouponSummary(
    val id: Long?,
    val name: String,
    val discountPercentage: Long,
    val validDays: Int?,
) {
    fun calculateExpiredAt(now: LocalDateTime): LocalDateTime? = validDays?.let { now.plusDays(it.toLong()) }
}
