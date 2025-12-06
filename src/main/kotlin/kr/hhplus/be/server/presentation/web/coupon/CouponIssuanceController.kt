package kr.hhplus.be.server.presentation.web.coupon

import kr.hhplus.be.server.application.usecase.coupon.CouponIssuanceUseCase
import kr.hhplus.be.server.presentation.dto.coupon.CouponIssuanceRequest
import kr.hhplus.be.server.presentation.dto.coupon.CouponIssuanceResponse
import kr.hhplus.be.server.presentation.web.idempotency.IdempotentContext
import kr.hhplus.be.server.presentation.web.idempotency.IdempotentRequestContext
import kr.hhplus.be.server.presentation.web.idempotency.IdempotentRequestResponseRecorder
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 쿠폰발급 API
 */
@RestController
@RequestMapping("/api/v1/coupons/issuance")
class CouponIssuanceController(
    private val couponIssuanceUseCase: CouponIssuanceUseCase,
    private val idempotentRequestResponseRecorder: IdempotentRequestResponseRecorder,
) {
    @PostMapping
    fun issue(
        @RequestHeader("X-Member-Id") memberId: Long,
        @RequestBody requestData: CouponIssuanceRequest,
        @IdempotentContext context: IdempotentRequestContext<CouponIssuanceResponse>,
    ): CouponIssuanceResponse =
        context.getResponseOrElse {
            val coupon =
                couponIssuanceUseCase.issue(
                    memberId = memberId,
                    couponSummaryId = requestData.couponSummaryId,
                )

            val response = CouponIssuanceResponse(coupon)

            idempotentRequestResponseRecorder.recordSuccess(
                cacheKey = context.cacheKey,
                responseBody = response,
                statusCode = HttpStatus.OK.value(),
                responseType = CouponIssuanceResponse::class.java,
            )
        }
}
