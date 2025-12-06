package kr.hhplus.be.server.presentation.web.coupon

import kr.hhplus.be.server.application.usecase.coupon.CouponIssuanceUseCase
import kr.hhplus.be.server.presentation.dto.coupon.CouponIssuanceRequest
import kr.hhplus.be.server.presentation.dto.coupon.CouponIssuanceResponse
import org.springframework.web.bind.annotation.*

/**
 * 쿠폰발급 API
 */
@RestController
@RequestMapping("/api/v1/coupons/issuance")
class CouponIssuanceController(
    private val couponIssuanceUseCase: CouponIssuanceUseCase,
) {
    @PostMapping
    fun issue(
        @RequestHeader("X-Member-Id") memberId: Long,
        @RequestBody requestData: CouponIssuanceRequest,
    ): CouponIssuanceResponse =
        CouponIssuanceResponse(
            couponIssuanceUseCase.issue(
                memberId = memberId,
                couponSummaryId = requestData.couponSummaryId,
            ),
        )
}
