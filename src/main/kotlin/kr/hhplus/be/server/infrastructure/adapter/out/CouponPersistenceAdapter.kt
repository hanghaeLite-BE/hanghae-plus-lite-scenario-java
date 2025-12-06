package kr.hhplus.be.server.infrastructure.adapter.out

import kr.hhplus.be.server.application.port.out.CouponOutput
import kr.hhplus.be.server.domain.model.coupon.Coupon
import kr.hhplus.be.server.infrastructure.persistence.coupon.couponJpaRepository
import kr.hhplus.be.server.infrastructure.persistence.coupon.mapper.CouponJpaEntityMapper
import kr.hhplus.be.server.infrastructure.persistence.coupon.mapper.CouponSummaryJpaEntityMapper
import org.springframework.stereotype.Component
import java.util.*

@Component
class CouponPersistenceAdapter(
    private val couponJpaRepository: couponJpaRepository,
) : CouponOutput {
    override fun findByIdAndMemberId(
        couponSummaryId: Long,
        memberId: Long,
    ): Optional<Coupon> =
        couponJpaRepository
            .findByIdAndMemberId(couponSummaryId, memberId)
            .map {
                CouponJpaEntityMapper.toDomain(
                    entity = it,
                    couponSummaryEntityToDomain = CouponSummaryJpaEntityMapper::toDomain,
                )
            }

    override fun findAllByMemberId(memberId: Long): List<Coupon> =
        couponJpaRepository
            .findAllByMemberId(memberId)
            .map {
                CouponJpaEntityMapper.toDomain(
                    entity = it,
                    couponSummaryEntityToDomain = CouponSummaryJpaEntityMapper::toDomain,
                )
            }

    override fun save(coupon: Coupon): Coupon {
        val entity = CouponJpaEntityMapper.toEntity(
            domain = coupon,
            couponSummaryDomainToEntity = CouponSummaryJpaEntityMapper::toEntity,
        ).let(couponJpaRepository::save)

        return CouponJpaEntityMapper.toDomain(
            entity = entity,
            couponSummaryEntityToDomain = CouponSummaryJpaEntityMapper::toDomain,
        )
    }
}
