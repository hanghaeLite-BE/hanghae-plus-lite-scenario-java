package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.application.concert.SeatRepositoryPort;
import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.reservation.Reservation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ConcurrencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationRepositoryPort reservationRepository;

    @Autowired
    private SeatRepositoryPort seatRepository;

    @Test
    @DisplayName("TC-LOCK-001: 동일 좌석 1개에 대해 50명이 동시에 예약 요청 -> 성공 1건, 실패 49건")
    @Sql(scripts = "/setup-concurrency.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void tcLock001() throws Exception {
        // given
        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // when
        for (int i = 1; i <= threadCount; i++) {
            final int userId = i;
            final String token = "token-" + i;
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    mockMvc.perform(post("/api/reservations")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(String.format("{\"userId\": %d, \"seatId\": 1, \"token\": \"%s\"}", userId, token)))
                            .andExpect(result -> {
                                int status = result.getResponse().getStatus();
                                if (status == 200 || status == 201) {
                                    successCount.getAndIncrement();
                                } else {
                                    failCount.getAndIncrement();
                                }
                            });
                } catch (Exception e) {
                    failCount.getAndIncrement();
                }
            }, executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();

        // then
        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(reservations).hasSize(1);
        assertThat(failCount.get()).isEqualTo(49);
    }

    @Test
    @DisplayName("TC-LOCK-002: 좌석 수가 20개인 경우 50명이 동시에 각각 랜덤하게 요청 -> 성공은 최대 20건")
    @Sql(scripts = "/setup-concurrency.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void tcLock002() throws Exception {
        // given
        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // when
        for (int i = 1; i <= threadCount; i++) {
            final int userId = i;
            final String token = "token-" + i;
            // 11~30번 좌석 중 하나를 선택 (동일 좌석 경쟁도 발생하도록 함)
            final long seatId = 11 + (i % 20); 
            
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    mockMvc.perform(post("/api/reservations")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(String.format("{\"userId\": %d, \"seatId\": %d, \"token\": \"%s\"}", userId, seatId, token)))
                            .andExpect(result -> {
                                int status = result.getResponse().getStatus();
                                if (status == 200 || status == 201) {
                                    successCount.getAndIncrement();
                                } else {
                                    failCount.getAndIncrement();
                                }
                            });
                } catch (Exception e) {
                    failCount.getAndIncrement();
                }
            }, executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();

        // then
        List<Reservation> reservations = reservationRepository.findAll();
        // 이론상 최대 20명 성공 가능 (좌석이 20개이므로)
        assertThat(successCount.get()).isLessThanOrEqualTo(20);
        assertThat(reservations.size()).isEqualTo(successCount.get());
    }
}

