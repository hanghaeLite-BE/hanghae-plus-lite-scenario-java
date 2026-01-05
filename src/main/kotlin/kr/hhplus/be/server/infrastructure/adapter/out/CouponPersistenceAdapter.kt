package kr.hhplus.be.server.infrastructure.adapter.out

import jakarta.persistence.EntityManager
import kr.hhplus.be.server.application.port.out.CouponOutput
import kr.hhplus.be.server.domain.exception.ConflictResourceException
import kr.hhplus.be.server.domain.model.coupon.Coupon
import kr.hhplus.be.server.infrastructure.persistence.coupon.CouponJpaRepository
import kr.hhplus.be.server.infrastructure.persistence.coupon.mapper.CouponJpaEntityMapper
import kr.hhplus.be.server.infrastructure.persistence.coupon.mapper.CouponSummaryJpaEntityMapper
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

@Component
class CouponPersistenceAdapter(
    private val couponJpaRepository: CouponJpaRepository,
    private val entityManager: EntityManager,
) : CouponOutput {
    override fun findByCouponSummaryIdAndMemberId(
        couponSummaryId: Long,
        memberId: Long,
    ): Optional<Coupon> =
        couponJpaRepository
            .findByCouponSummaryIdAndMemberId(couponSummaryId, memberId)
            .map {
                CouponJpaEntityMapper.toDomain(
                    entity = it,
                    couponSummaryEntityToDomain = CouponSummaryJpaEntityMapper::toDomain,
                )
            }

    override fun findAllByMemberId(memberId: Long): List<Coupon> =
        couponJpaRepository
            .findAllWithSummaryByMemberId(memberId)
            .map {
                CouponJpaEntityMapper.toDomain(
                    entity = it,
                    couponSummaryEntityToDomain = CouponSummaryJpaEntityMapper::toDomain,
                )
            }

    override fun save(coupon: Coupon): Coupon {
        try {
            val entity =
                CouponJpaEntityMapper
                    .toEntity(
                        domain = coupon,
                        couponSummaryDomainToEntity = CouponSummaryJpaEntityMapper::toEntity,
                    ).let(couponJpaRepository::save)

            return CouponJpaEntityMapper.toDomain(
                entity = entity,
                couponSummaryEntityToDomain = CouponSummaryJpaEntityMapper::toDomain,
            )
        } catch (e: DataIntegrityViolationException) {
            throw ConflictResourceException(
                cause = e,
                message = "이미 발급된 쿠폰입니다.",
                clue =
                    mapOf(
                        "memberId" to "${coupon.memberId}",
                        "couponSummaryId" to "${coupon.couponSummary.id}",
                    ),
            )
        }
    }

    override fun atomicUse(
        couponSummaryId: Long,
        memberId: Long,
        now: LocalDateTime,
    ): Boolean = couponJpaRepository.atomicUse(couponSummaryId, memberId, now) > 0

    override fun refresh(coupon: Coupon): Coupon {
        val entity =
            couponJpaRepository
                .findById(coupon.id!!)
                .getOrNull()!!

        entityManager.refresh(entity)

        return CouponJpaEntityMapper.toDomain(
            entity = entity,
            couponSummaryEntityToDomain = CouponSummaryJpaEntityMapper::toDomain,
        )
    }
}
