package kr.hhplus.be.server.domain.booking;

import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.concert.SeatRepository;
import kr.hhplus.be.server.domain.member.Member;
import kr.hhplus.be.server.domain.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    @DisplayName("예약 및 결제 성공 테스트 (얕은 검증)")
    void confirmBooking_Success() {
        // given
        Long memberId = 1L;
        Long seatId = 1L;
        Member member = new Member("사용자");
        member.setPoint(10000L);
        Seat seat = new Seat(1L, 1, 5000L);
        seat.setStatus(Seat.SeatStatus.AVAILABLE);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(seatRepository.findById(seatId)).willReturn(Optional.of(seat));
        given(reservationRepository.save(any())).willReturn(new Reservation(memberId, seatId));

        // when
        Reservation result = bookingService.confirmBooking(memberId, seatId);

        // then
        assertThat(result).isNotNull();
        // 비즈니스 로직 결과물(포인트 차감액, 좌석 상태 변경 등)에 대한 세밀한 assert 대신 save 호출 여부 위주로 확인
        verify(seatRepository, times(1)).save(any());
        verify(reservationRepository, times(1)).save(any());
        verify(memberRepository, times(1)).save(any());
        verify(paymentRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("좌석이 AVAILABLE이 아니면 실패한다")
    void confirmBooking_Fail_AlreadyReserved() {
        // given
        Long memberId = 1L;
        Long seatId = 1L;
        Member member = new Member("사용자");
        Seat seat = new Seat(1L, 1, 5000L);
        seat.setStatus(Seat.SeatStatus.RESERVED); // 이미 예약된 상태

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(seatRepository.findById(seatId)).willReturn(Optional.of(seat));

        // when & then
        assertThatThrownBy(() -> bookingService.confirmBooking(memberId, seatId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("이미 예약되었거나 판매된 좌석입니다.");
    }
}
