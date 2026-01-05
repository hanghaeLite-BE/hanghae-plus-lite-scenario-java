package kr.hhplus.be.server.infrastructure.adapter.out

import kr.hhplus.be.server.application.port.out.CouponIssuancePolicyOutput
import kr.hhplus.be.server.domain.model.coupon.policy.CouponIssuancePolicy
import kr.hhplus.be.server.infrastructure.persistence.coupon.mapper.CouponIssuanceJpaEntityMapper
import kr.hhplus.be.server.infrastructure.persistence.coupon.mapper.CouponIssuancePolicyJpaEntityMapper
import kr.hhplus.be.server.infrastructure.persistence.coupon.policy.CouponIssuancePolicyJpaRepository
import org.springframework.stereotype.Component

@Component
class CouponIssuancePolicyPersistenceAdapter(
    private val issuancePolicyRepository: CouponIssuancePolicyJpaRepository,
) : CouponIssuancePolicyOutput {
    override fun findAllByCouponIssuanceId(couponIssuanceId: Long): List<CouponIssuancePolicy> {
        val entityList = issuancePolicyRepository.findAllByCouponIssuanceJpaEntity_Id(couponIssuanceId)
        return entityList.map {
            CouponIssuancePolicyJpaEntityMapper.toDomain(
                entity = it,
                couponIssuanceEntityTo = CouponIssuanceJpaEntityMapper::toDomain,
            )
        }
    }
}
