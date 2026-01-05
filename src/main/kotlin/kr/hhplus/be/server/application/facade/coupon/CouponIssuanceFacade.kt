package kr.hhplus.be.server.application.facade.coupon

import kr.hhplus.be.server.application.port.out.CouponIssuanceOutput
import kr.hhplus.be.server.application.port.out.CouponIssuancePolicyOutput
import kr.hhplus.be.server.application.port.out.CouponOutput
import kr.hhplus.be.server.application.port.out.CouponSummaryOutput
import kr.hhplus.be.server.application.usecase.coupon.CouponIssuancePolicyUseCaseFactory
import kr.hhplus.be.server.application.usecase.coupon.CouponIssuanceUseCase
import kr.hhplus.be.server.domain.exception.ConflictResourceException
import kr.hhplus.be.server.domain.exception.NotFoundResourceException
import kr.hhplus.be.server.domain.model.coupon.Coupon
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CouponIssuanceFacade(
    private val couponOutput: CouponOutput,
    private val summaryOutput: CouponSummaryOutput,
    private val issuanceOutput: CouponIssuanceOutput,
    private val issuancePolicyOutput: CouponIssuancePolicyOutput,
    private val issuancePolicyUseCaseFactory: CouponIssuancePolicyUseCaseFactory,
) : CouponIssuanceUseCase {
    /**
     * 쿠폰을 발급한다
     *
     * - 쿠폰정보를 조회하여 존재하는지 확인한다.
     * - 쿠폰발급정보를 조회한다.
     * - 쿠폰발급정책들을 조회하여 순환하며 발급가능여부를 확인한다.
     * - 조건부 UPDATE로 쿠폰발급수량을 원자적으로 증가시킨다.
     * - 쿠폰을 생성하여 저장한다. (Unique 제약으로 중복 발급 방지)
     * - 저장된 쿠폰을 반환한다.
     */
    @Transactional
    override fun issue(
        memberId: Long,
        couponSummaryId: Long,
        now: LocalDateTime,
    ): Coupon {
        val summary =
            summaryOutput
                .findById(couponSummaryId)
                .orElseThrow {
                    NotFoundResourceException(
                        message = "요청하신 쿠폰의 정보를 찾을수없습니다.",
                        clue = mapOf("couponSummaryId" to couponSummaryId),
                    )
                }

        val issuance =
            issuanceOutput
                .findByCouponSummaryId(couponSummaryId)
                .orElseThrow {
                    NotFoundResourceException(
                        message = "요청하신 쿠폰의 발급정보를 찾을수없습니다.",
                        clue = mapOf("couponSummaryId" to couponSummaryId),
                    )
                }

        issuancePolicyOutput
            .findAllByCouponIssuanceId(issuance.id!!)
            .mapNotNull(issuancePolicyUseCaseFactory::from)
            .forEach { it.canIssue(memberId, summary) }

        if (!issuanceOutput.atomicIssue(couponSummaryId, now)) {
            throw ConflictResourceException(
                message = "쿠폰 발급에 실패했습니다. 수량이 소진되었거나 발급 기간이 아닙니다.",
                clue =
                    mapOf(
                        "couponSummaryId" to couponSummaryId,
                        "now" to now,
                    ),
            )
        }

        return Coupon
            .create(
                memberId = memberId,
                couponSummary = summary,
                now = now,
            ).let(couponOutput::save)
    }
}
