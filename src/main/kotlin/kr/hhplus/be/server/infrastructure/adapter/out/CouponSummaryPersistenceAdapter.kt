package kr.hhplus.be.server.infrastructure.adapter.out

import kr.hhplus.be.server.application.port.out.CouponSummaryOutput
import kr.hhplus.be.server.domain.model.coupon.CouponSummary
import kr.hhplus.be.server.infrastructure.persistence.coupon.CouponSummaryJpaRepository
import kr.hhplus.be.server.infrastructure.persistence.coupon.mapper.CouponSummaryJpaEntityMapper
import org.springframework.stereotype.Component
import java.util.*

@Component
class CouponSummaryPersistenceAdapter(
    private val couponSummaryJpaRepository: CouponSummaryJpaRepository,
) : CouponSummaryOutput {
    override fun existsById(id: Long): Boolean {
        return couponSummaryJpaRepository.existsById(id)
    }

    override fun findById(id: Long): Optional<CouponSummary> {
        return couponSummaryJpaRepository.findById(id)
            .map(CouponSummaryJpaEntityMapper::toDomain)
    }
}
