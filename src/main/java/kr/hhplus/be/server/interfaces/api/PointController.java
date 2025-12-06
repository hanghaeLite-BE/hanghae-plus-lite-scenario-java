package kr.hhplus.be.server.interfaces.api;

import kr.hhplus.be.server.application.service.PointService;
import kr.hhplus.be.server.domain.entity.Point;
import kr.hhplus.be.server.domain.entity.PointHistory;
import kr.hhplus.be.server.interfaces.dto.PointChargeRequest;
import kr.hhplus.be.server.interfaces.dto.PointResponse;
import kr.hhplus.be.server.interfaces.dto.PointHistoryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/points")
public class PointController {

    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    /**
     * 포인트 충전
     */
    @PostMapping("/charge")
    public ResponseEntity<PointResponse> chargePoint(@RequestBody @Valid PointChargeRequest request) {
        Point point = pointService.chargePoint(
                request.getUserId(),
                request.getAmount(),
                request.getDescription()
        );

        return ResponseEntity.ok(PointResponse.from(point));
    }

    /**
     * 포인트 잔액 조회
     */
    @GetMapping("/balance/{userId}")
    public ResponseEntity<PointResponse> getPointBalance(@PathVariable Long userId) {
        Point point = pointService.getPointBalance(userId);
        return ResponseEntity.ok(PointResponse.from(point));
    }

    /**
     * 포인트 이력 조회
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<PointHistoryResponse>> getPointHistory(@PathVariable Long userId) {
        List<PointHistory> histories = pointService.getPointHistory(userId);
        List<PointHistoryResponse> responses = histories.stream()
                .map(PointHistoryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 포인트 사용 가능 여부 확인
     */
    @GetMapping("/can-use/{userId}")
    public ResponseEntity<Boolean> canUsePoints(@PathVariable Long userId,
                                                @RequestParam java.math.BigDecimal amount) {
        boolean canUse = pointService.canUsePoints(userId, amount);
        return ResponseEntity.ok(canUse);
    }
}
