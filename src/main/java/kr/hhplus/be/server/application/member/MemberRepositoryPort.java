package kr.hhplus.be.server.application.member;

import kr.hhplus.be.server.domain.member.Member;
import java.util.Optional;

public interface MemberRepositoryPort {
    Optional<Member> findById(Long id);
    Optional<Member> findByIdWithLock(Long id);
    void save(Member member);
}
