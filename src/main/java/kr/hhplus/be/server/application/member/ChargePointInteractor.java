package kr.hhplus.be.server.application.member;

import kr.hhplus.be.server.domain.member.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChargePointInteractor implements ChargePointUseCase {

    private final MemberRepositoryPort memberRepository;

    public ChargePointInteractor(MemberRepositoryPort memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional
    public void execute(Long memberId, Long amount) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        member.charge(amount);
        memberRepository.save(member);
    }
}
