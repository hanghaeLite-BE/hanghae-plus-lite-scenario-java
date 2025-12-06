package kr.hhplus.be.server.interfaces;

import kr.hhplus.be.server.application.GetAvailableDatesService;
import kr.hhplus.be.server.application.GetConcertSeatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/concerts")
public class ConcertController {
    private final GetAvailableDatesService getAvailableDatesService;
    private final GetConcertSeatsService getConcertSeatsService;

    public ConcertController(GetAvailableDatesService getAvailableDatesService,
                             GetConcertSeatsService getConcertSeatsService) {
        this.getAvailableDatesService = getAvailableDatesService;
        this.getConcertSeatsService = getConcertSeatsService;
    }

    @GetMapping("/available-dates")
    public ResponseEntity<ApiResponse<AvailableDatesResponse>> getAvailableDates(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Integer limit,
            @RequestHeader("Authorization") String queueToken) {

        // 대기열 토큰 검증 로직 (별도 구현 필요)
        validateQueueToken(queueToken);

        List<GetAvailableDatesService.AvailableDateResponse> availableDates =
                getAvailableDatesService.getAvailableDates(month, limit);

        AvailableDatesResponse response = new AvailableDatesResponse(availableDates);

        return ResponseEntity.ok(ApiResponse.success(response, "예약 가능 날짜 조회 성공"));
    }

    @GetMapping("/{concertId}/seats")
    public ResponseEntity<ApiResponse<GetConcertSeatsService.ConcertSeatsResponse>> getConcertSeats(
            @PathVariable String concertId,
            @RequestParam String date,
            @RequestHeader("Authorization") String queueToken) {

        // 대기열 토큰 검증 로직 (별도 구현 필요)
        validateQueueToken(queueToken);

        GetConcertSeatsService.ConcertSeatsResponse response =
                getConcertSeatsService.getConcertSeats(concertId, date);

        return ResponseEntity.ok(ApiResponse.success(response, "좌석 정보 조회 성공"));
    }

    private void validateQueueToken(String queueToken) {
        // 대기열 토큰 검증 로직
        // 실제로는 별도의 QueueService에서 처리
        if (queueToken == null || !queueToken.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 대기열 토큰입니다.");
        }
    }

    // Response 클래스들
    public static class AvailableDatesResponse {
        private final List<GetAvailableDatesService.AvailableDateResponse> availableDates;

        public AvailableDatesResponse(List<GetAvailableDatesService.AvailableDateResponse> availableDates) {
            this.availableDates = availableDates;
        }

        public List<GetAvailableDatesService.AvailableDateResponse> getAvailableDates() {
            return availableDates;
        }
    }

    public static class ApiResponse<T> {
        private final boolean success;
        private final T data;
        private final String message;
        private final String timestamp;

        private ApiResponse(boolean success, T data, String message) {
            this.success = success;
            this.data = data;
            this.message = message;
            this.timestamp = java.time.Instant.now().toString();
        }

        public static <T> ApiResponse<T> success(T data, String message) {
            return new ApiResponse<>(true, data, message);
        }

        public boolean isSuccess() { return success; }
        public T getData() { return data; }
        public String getMessage() { return message; }
        public String getTimestamp() { return timestamp; }
    }
}

