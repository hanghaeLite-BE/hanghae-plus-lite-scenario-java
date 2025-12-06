package kr.hhplus.be.server.infrastructure.adapter.out

import kr.hhplus.be.server.application.port.out.CouponIssuanceOutput
import kr.hhplus.be.server.domain.model.coupon.CouponIssuance
import kr.hhplus.be.server.infrastructure.persistence.coupon.CouponIssuanceJpaRepository
import kr.hhplus.be.server.infrastructure.persistence.coupon.mapper.CouponIssuanceJpaEntityMapper
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class CouponIssuancePersistenceAdapter(
    private val couponIssuanceJpaRepository: CouponIssuanceJpaRepository,
) : CouponIssuanceOutput {
    override fun findByCouponSummaryIdWithLock(couponSummaryId: Long): Optional<CouponIssuance> =
        couponIssuanceJpaRepository
            .findByCouponSummaryIdWithLock(couponSummaryId)
            .map(CouponIssuanceJpaEntityMapper::toDomain)

    override fun save(domain: CouponIssuance): CouponIssuance {
        val savedEntity =
            CouponIssuanceJpaEntityMapper
                .toEntity(domain)
                .let(couponIssuanceJpaRepository::save)

        return CouponIssuanceJpaEntityMapper.toDomain(savedEntity)
    }
}
