package kr.hhplus.be.server;

import kr.hhplus.be.server.application.service.ConcertService;
import kr.hhplus.be.server.domain.model.Concert;
import kr.hhplus.be.server.domain.model.ConcertStatus;
import kr.hhplus.be.server.domain.repository.ConcertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConcertServiceTest {

    @Mock
    private ConcertRepository concertRepository;

    @InjectMocks
    private ConcertService concertService;

    private Concert testConcert;

    @BeforeEach
    void setUp() {
        testConcert = Concert.create(
                "테스트 콘서트",
                "테스트 아티스트",
                LocalDateTime.now().plusDays(7),
                50,
                50000
        );
    }

    @Test
    @DisplayName("예약 가능한 콘서트 목록을 조회할 수 있다")
    void getAvailableConcerts() {
        // Given
        List<Concert> mockConcerts = Arrays.asList(testConcert);
        when(concertRepository.findAvailableConcerts(eq(ConcertStatus.AVAILABLE), any(LocalDateTime.class)))
                .thenReturn(mockConcerts);

        // When
        List<Concert> result = concertService.getAvailableConcerts();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("테스트 콘서트");
        verify(concertRepository).findAvailableConcerts(eq(ConcertStatus.AVAILABLE), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("콘서트 ID로 상세 정보를 조회할 수 있다")
    void getConcertById() {
        // Given
        Long concertId = 1L;
        when(concertRepository.findById(concertId)).thenReturn(Optional.of(testConcert));

        // When
        Concert result = concertService.getConcertById(concertId);

        // Then
        assertThat(result.getTitle()).isEqualTo("테스트 콘서트");
        assertThat(result.getArtist()).isEqualTo("테스트 아티스트");
        verify(concertRepository).findById(concertId);
    }

    @Test
    @DisplayName("존재하지 않는 콘서트 ID로 조회시 예외가 발생한다")
    void getConcertById_NotFound() {
        // Given
        Long concertId = 999L;
        when(concertRepository.findById(concertId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> concertService.getConcertById(concertId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("콘서트를 찾을 수 없습니다: " + concertId);

        verify(concertRepository).findById(concertId);
    }

    @Test
    @DisplayName("날짜 범위로 콘서트를 조회할 수 있다")
    void getConcertsByDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(30);
        List<Concert> mockConcerts = Arrays.asList(testConcert);

        when(concertRepository.findConcertsByDateRange(startDate, endDate))
                .thenReturn(mockConcerts);

        // When
        List<Concert> result = concertService.getConcertsByDateRange(startDate, endDate);

        // Then
        assertThat(result).hasSize(1);
        verify(concertRepository).findConcertsByDateRange(startDate, endDate);
    }

    @Test
    @DisplayName("시작 날짜가 종료 날짜보다 늦으면 예외가 발생한다")
    void getConcertsByDateRange_InvalidDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().plusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        // When & Then
        assertThatThrownBy(() -> concertService.getConcertsByDateRange(startDate, endDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
    }

    @Test
    @DisplayName("아티스트명으로 콘서트를 조회할 수 있다")
    void getConcertsByArtist() {
        // Given
        String artist = "테스트 아티스트";
        List<Concert> mockConcerts = Arrays.asList(testConcert);

        when(concertRepository.findByArtistAndFutureDates(eq(artist), any(LocalDateTime.class)))
                .thenReturn(mockConcerts);

        // When
        List<Concert> result = concertService.getConcertsByArtist(artist);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getArtist()).isEqualTo(artist);
        verify(concertRepository).findByArtistAndFutureDates(eq(artist), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("아티스트명이 비어있으면 예외가 발생한다")
    void getConcertsByArtist_EmptyArtist() {
        // When & Then
        assertThatThrownBy(() -> concertService.getConcertsByArtist(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("아티스트명은 필수입니다.");

        assertThatThrownBy(() -> concertService.getConcertsByArtist(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("아티스트명은 필수입니다.");
    }
}

