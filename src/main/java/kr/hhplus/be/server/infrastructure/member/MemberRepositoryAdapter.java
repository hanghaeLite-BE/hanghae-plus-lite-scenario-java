package kr.hhplus.be.server.infrastructure.member;

import kr.hhplus.be.server.application.member.MemberRepositoryPort;
import kr.hhplus.be.server.domain.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryAdapter implements MemberRepositoryPort {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberMapper memberMapper;

    @Override
    public Optional<Member> findById(Long id) {
        // 아쉬운 점: findById를 호출하지만 실제로는 MemberEntity가 반환되고 이를 도메인으로 변환하는 과정에서 
        // 데이터 베이스의 dirty checking 등의 혜택을 보기 위해 엔티티를 직접 노출하고 싶은 유혹을 느낌
        return memberJpaRepository.findById(id)
                .map(memberMapper::toDomain);
    }

    @Override
    public Member save(Member member) {
        MemberEntity entity = memberMapper.toEntity(member);
        return memberMapper.toDomain(memberJpaRepository.save(entity));
    }
}
