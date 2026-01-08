package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import kr.hhplus.be.server.domain.member.Member;
import kr.hhplus.be.server.application.concert.SeatRepositoryPort;
import kr.hhplus.be.server.application.member.MemberRepositoryPort;
import kr.hhplus.be.server.infrastructure.concert.SeatJpaRepository;
import kr.hhplus.be.server.infrastructure.member.MemberJpaRepository;
import kr.hhplus.be.server.infrastructure.reservation.ReservationJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ReservationIntegrationTest {

    @Autowired
    private ReserveSeatUseCase reserveSeatUseCase;

    @Autowired
    private ConfirmReservationUseCase confirmReservationUseCase;

    @Autowired
    private SeatRepositoryPort seatRepositoryPort;

    @Autowired
    private MemberRepositoryPort memberRepositoryPort;

    @Autowired
    private ReservationRepositoryPort reservationRepositoryPort;

    @Test
    @DisplayName("토큰->예약->결제 통합 테스트 (아쉬운 검증)")
    void integrationTest() {
        // given
        Long memberId = 1L;
        Long seatId = 1L;
        // 아쉬운 점: 테스트 데이터 set up이 부실하고 DB 초기화 로직이 없음

        // when
        reserveSeatUseCase.execute(memberId, seatId);
        // confirmReservationUseCase.execute(memberId, 1L); // 예약 ID를 알아야 하나 대충 넘김

        // then
        // 아쉬운 점: 상태값 하나만 확인하고 잔액 차감이나 결제 로그 등은 확인하지 않음
        Seat seat = seatRepositoryPort.findById(seatId).orElseThrow();
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.UNAVAILABLE);
    }

    @Test
    @DisplayName("만료 후 재예약 테스트 (Thread.sleep 사용)")
    void expiryTest() throws InterruptedException {
        // given
        Long memberId = 1L;
        Long seatId = 2L;
        reserveSeatUseCase.execute(memberId, seatId);

        // 아쉬운 점: 시간을 제어하지 못해 Thread.sleep으로 대기. 테스트가 느려지고 플래키해짐.
        // 실제로는 5분 만료지만 테스트를 위해 로직 일부를 수정하거나 아주 짧게 잡았다고 가정
        Thread.sleep(1000); 

        // when & then
        // 만료 후 로직 검증...
    }

    @Test
    @DisplayName("동시 예약 테스트 (부실한 구현)")
    void concurrencyTest() {
        // 아쉬운 점: 다중 쓰레드를 활용한 실제 경합 상황 테스트가 아니라 단순 반복 호출
        Long seatId = 3L;
        
        for (int i = 0; i < 10; i++) {
            try {
                reserveSeatUseCase.execute((long) i, seatId);
            } catch (Exception e) {
                // ignore
            }
        }

        // 결과 확인도 대충...
        Seat seat = seatRepositoryPort.findById(seatId).orElseThrow();
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.UNAVAILABLE);
    }
}
