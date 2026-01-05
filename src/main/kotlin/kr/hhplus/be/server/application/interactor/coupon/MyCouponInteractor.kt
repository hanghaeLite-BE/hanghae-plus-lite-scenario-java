package kr.hhplus.be.server.application.interactor.coupon

import kr.hhplus.be.server.application.port.out.CouponOutput
import kr.hhplus.be.server.application.usecase.coupon.MyCouponUseCase
import kr.hhplus.be.server.application.vo.CouponItemVO
import kr.hhplus.be.server.domain.exception.ConflictResourceException
import kr.hhplus.be.server.domain.exception.NotFoundResourceException
import kr.hhplus.be.server.domain.model.coupon.Coupon
import kr.hhplus.be.server.domain.model.member.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class MyCouponInteractor(
    private val couponOutput: CouponOutput,
) : MyCouponUseCase {
    @Transactional(readOnly = true)
    override fun getMyCoupons(memberId: Long): List<CouponItemVO> =
        couponOutput.findAllByMemberId(memberId).map {
            CouponItemVO(
                id = it.id!!,
                name = it.couponSummary.name,
                discountPercentage = it.couponSummary.discountPercentage,
                expiredAt = it.expiredAt,
                usingAt = it.usingAt,
            )
        }

    @Transactional
    override fun using(
        member: Member,
        couponSummaryId: Long,
        now: LocalDateTime,
    ): Coupon {
        val memberId = member.id!!
        val coupon =
            couponOutput
                .findByCouponSummaryIdAndMemberId(couponSummaryId, memberId)
                .orElseThrow {
                    throw NotFoundResourceException(
                        message = "보유하지 않은 쿠폰입니다.",
                        clue = mapOf("memberId" to "$memberId", "couponSummaryId" to "$couponSummaryId"),
                    )
                }

        if (!couponOutput.atomicUse(couponSummaryId, memberId, now)) {
            throw ConflictResourceException(
                message = "이미 사용되었거나 만료된 쿠폰입니다.",
                clue = mapOf("memberId" to "$memberId", "couponSummaryId" to "$couponSummaryId"),
            )
        }

        return couponOutput.refresh(coupon)
    }
}
