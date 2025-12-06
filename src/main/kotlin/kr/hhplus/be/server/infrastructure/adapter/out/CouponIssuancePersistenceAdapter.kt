package kr.hhplus.be.server.infrastructure.adapter.out

import kr.hhplus.be.server.application.port.out.CouponIssuanceOutput
import kr.hhplus.be.server.domain.model.coupon.CouponIssuance
import kr.hhplus.be.server.infrastructure.persistence.coupon.CouponIssuanceJpaRepository
import kr.hhplus.be.server.infrastructure.persistence.coupon.mapper.CouponIssuanceJpaEntityMapper
import org.springframework.stereotype.Component
import java.util.*

@Component
class CouponIssuancePersistenceAdapter(
    private val couponIssuanceJpaRepository: CouponIssuanceJpaRepository,
) : CouponIssuanceOutput {
    override fun findByCouponSummaryId(couponSummaryId: Long): Optional<CouponIssuance> {
        return couponIssuanceJpaRepository.findByCouponSummaryId(couponSummaryId)
            .map(CouponIssuanceJpaEntityMapper::toDomain)
    }

    override fun save(couponIssuance: CouponIssuance): CouponIssuance {
        val savedEntity = CouponIssuanceJpaEntityMapper
            .toEntity(couponIssuance)
            .let(couponIssuanceJpaRepository::save)

        return CouponIssuanceJpaEntityMapper.toDomain(savedEntity)
    }
}
