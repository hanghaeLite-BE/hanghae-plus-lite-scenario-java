package kr.hhplus.be.server.presentation.web.coupon

import kr.hhplus.be.server.application.usecase.coupon.MyCouponUseCase
import kr.hhplus.be.server.presentation.dto.coupon.MyCouponsResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/coupons/me")
class MyCouponController(
    private val myCouponUseCase: MyCouponUseCase,
) {
    @GetMapping
    fun getMyCoupons(
        @RequestHeader("X-Member-Id") memberId: Long,
    ): MyCouponsResponse {
        val coupons = myCouponUseCase.getMyCoupons(memberId)
        return MyCouponsResponse(coupons)
    }
}
