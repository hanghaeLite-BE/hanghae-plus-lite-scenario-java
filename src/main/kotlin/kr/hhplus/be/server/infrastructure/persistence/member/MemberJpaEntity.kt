package kr.hhplus.be.server.infrastructure.persistence.member

import jakarta.persistence.*

@Entity
@Table(name = "member")
class MemberJpaEntity(
    @Column(nullable = false)
    val email: String,
    @Column(nullable = false)
    val pwd: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val memberType: MemberType = MemberType.GENERAL,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}

enum class MemberType {
    GENERAL,
    VIP,
    ADMIN,
    ;

    companion object {
        fun from(type: String): MemberType? =
            entries.find { it.name.equals(type, ignoreCase = true) }
    }
}
