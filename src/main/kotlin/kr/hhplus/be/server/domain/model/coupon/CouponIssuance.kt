package kr.hhplus.be.server.domain.model.coupon

import kr.hhplus.be.server.domain.exception.ConflictResourceException
import java.time.LocalDateTime

data class CouponIssuance(
    val id: Long?,
    val couponSummaryId: Long,
    val issuedCount: Int,
    val maxCount: Int?,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
) {
    fun issue(now: LocalDateTime = LocalDateTime.now()): CouponIssuance {
        validate(now)
        return this.copy(issuedCount = this.issuedCount + 1)
    }

    private fun validate(now: LocalDateTime) {
        if (now !in startAt..endAt) {
            throw ConflictResourceException(
                message = "쿠폰발급이 가능한 기간이 아닙니다.",
                clue =
                    mapOf(
                        "id" to "$id",
                        "now" to "$now",
                        "startAt" to "$startAt",
                        "endAt" to "$endAt",
                    ),
            )
        }

        if (maxCount != null && maxCount <= issuedCount) {
            throw ConflictResourceException(
                message = "쿠폰발급수량이 부족합니다.",
                clue =
                    mapOf(
                        "id" to "$id",
                        "issuedCont" to "$issuedCount",
                        "maxCount" to "$maxCount",
                    ),
            )
        }
    }
}
