package kr.hhplus.be.server.infrastructure.adapter.out

import kr.hhplus.be.server.application.port.out.CouponIssuanceOutput
import kr.hhplus.be.server.domain.model.coupon.CouponIssuance
import kr.hhplus.be.server.infrastructure.persistence.coupon.CouponIssuanceJpaRepository
import kr.hhplus.be.server.infrastructure.persistence.coupon.mapper.CouponIssuanceJpaEntityMapper
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.Optional

@Component
class CouponIssuancePersistenceAdapter(
    private val couponIssuanceJpaRepository: CouponIssuanceJpaRepository,
) : CouponIssuanceOutput {
    @Transactional(readOnly = true)
    override fun findByCouponSummaryId(couponSummaryId: Long): Optional<CouponIssuance> =
        couponIssuanceJpaRepository
            .findByCouponSummaryId(couponSummaryId)
            .map(CouponIssuanceJpaEntityMapper::toDomain)

    @Transactional
    override fun atomicIssue(
        couponSummaryId: Long,
        now: LocalDateTime,
    ): Boolean = couponIssuanceJpaRepository.atomicIssue(couponSummaryId, now) > 0
}
