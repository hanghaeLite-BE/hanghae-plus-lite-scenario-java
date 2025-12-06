package kr.hhplus.be.server.application.vo

import java.time.LocalDateTime

data class CouponItemVO(
    val id: Long,
    val name: String,
    val discountPercentage: Long,
    val expiredAt: LocalDateTime?,
    val usingAt: LocalDateTime?,
)
