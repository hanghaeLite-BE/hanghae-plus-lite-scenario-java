package kr.hhplus.be.server.infrastructure.member;

import kr.hhplus.be.server.application.member.MemberRepositoryPort;
import kr.hhplus.be.server.domain.member.Member;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MemberRepositoryAdapter implements MemberRepositoryPort {
    private final MemberJpaRepository memberJpaRepository;

    public MemberRepositoryAdapter(MemberJpaRepository memberJpaRepository) {
        this.memberJpaRepository = memberJpaRepository;
    }

    @Override
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id).map(MemberMapper::toDomain);
    }

    @Override
    public void save(Member member) {
        memberJpaRepository.save(MemberMapper.toEntity(member));
    }
}
