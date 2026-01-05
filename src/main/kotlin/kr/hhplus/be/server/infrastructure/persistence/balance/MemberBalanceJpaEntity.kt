package kr.hhplus.be.server.infrastructure.persistence.balance

import jakarta.persistence.*
import kr.hhplus.be.server.infrastructure.persistence.config.CreatedAndUpdatedAtAuditEntity
import kr.hhplus.be.server.infrastructure.persistence.member.MemberJpaEntity

@Entity
@Table(name = "member_balance")
class MemberBalanceJpaEntity(
    @OneToOne
    @JoinColumn(name = "member_id", nullable = false)
    val member: MemberJpaEntity,
) : CreatedAndUpdatedAtAuditEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var balance: Long = 0L
}
