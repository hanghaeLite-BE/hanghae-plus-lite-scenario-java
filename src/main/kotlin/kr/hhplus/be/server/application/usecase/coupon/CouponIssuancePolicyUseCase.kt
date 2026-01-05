package kr.hhplus.be.server.application.usecase.coupon

import kr.hhplus.be.server.domain.model.coupon.CouponSummary
import kr.hhplus.be.server.domain.model.coupon.policy.CouponIssuancePolicy

/**
 * 쿠폰정책 도메인 로직을 실행하는 usecase.
 * 쿠폰정책 도메인 구현체별로 검증에 필요로하는 값과 다른 도메인들이 다르기에 이 usecase에서
 * 각각 핋요한 outport를 의존하고 로직을 오케스트레이션합니다.
 */
interface CouponIssuancePolicyUseCase<T : CouponIssuancePolicy> {
    fun genericClassInfo(): Class<T>
    fun canIssue(memberId: Long, couponSummary: CouponSummary)
}
