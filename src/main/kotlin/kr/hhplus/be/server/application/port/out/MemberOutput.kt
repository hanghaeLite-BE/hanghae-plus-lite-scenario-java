package kr.hhplus.be.server.application.port.out

import kr.hhplus.be.server.domain.model.member.Member
import java.util.Optional

interface MemberOutput {
    fun findById(memberId: Long): Optional<Member>
}
