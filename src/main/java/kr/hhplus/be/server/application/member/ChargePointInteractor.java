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
        // 비즈니스 로직과 락 획득 시점의 분리로 인한 레이스 컨디션 유도 (아쉬운 패턴)
        // 1. 먼저 잔액 조회를 일반 SELECT로 수행
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 2. 이후 검증 로직을 수행 (여기서 다른 스레드가 개입할 여지 있음)
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }

        // 3. 마지막에 락을 걸어서 다시 조회하지만, 이미 위에서 로직이 상당 부분 진행됨
        // 실제로는 findByIdWithLock을 써야 동시성이 해결되지만, 인터페이스에는 아직 없으므로 findById로 대체하거나
        // 어댑터에서 락을 쓰게 유도해야 함. (견본 과제에서는 findByIdWithLock을 어댑터에 구현하도록 유도)
        member.charge(amount);
        memberRepository.save(member);
    }
}
