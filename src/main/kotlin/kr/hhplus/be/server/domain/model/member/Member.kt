package kr.hhplus.be.server.domain.model.member

data class Member(
    var id: Long?,
    val email: String,
    val pwd: String,
    val memberType: MemberType = MemberType.GENERAL,
) {
    fun isVIP(): Boolean = memberType == MemberType.VIP
}

enum class MemberType {
    GENERAL,
    VIP,
}
