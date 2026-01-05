package kr.hhplus.be.server.infrastructure.persistence.coupon.mapper

import io.mockk.mockk
import kr.hhplus.be.server.domain.model.coupon.CouponIssuance
import kr.hhplus.be.server.domain.model.coupon.policy.OnePerMemberPolicy
import kr.hhplus.be.server.infrastructure.persistence.coupon.CouponIssuanceJpaEntity
import kr.hhplus.be.server.infrastructure.persistence.coupon.policy.OnePerMemberPolicyJpaEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class CouponIssuancePolicyJpaEntityMapperTest {
    @Test
    @DisplayName("toDomain은 OnePerMemberPolicyJpaEntity를 OnePerMemberPolicy로 변환한다")
    fun toDomain_OnePerMemberPolicy() {
        val couponIssuanceEntity = mockk<CouponIssuanceJpaEntity>()
        val couponIssuance = mockk<CouponIssuance>()
        val entity = OnePerMemberPolicyJpaEntity(
            couponIssuanceJpaEntity = couponIssuanceEntity
        ).apply { id = 1L }
        val couponIssuanceEntityTo: (CouponIssuanceJpaEntity) -> CouponIssuance = { couponIssuance }

        val domain = CouponIssuancePolicyJpaEntityMapper.toDomain(entity, couponIssuanceEntityTo)

        assertEquals(entity.id, (domain as OnePerMemberPolicy).id)
        assertEquals(couponIssuance, domain.couponIssuance)
    }
}
