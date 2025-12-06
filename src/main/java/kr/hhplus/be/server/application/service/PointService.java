package kr.hhplus.be.server.application.service;

import kr.hhplus.be.server.domain.entity.Point;
import kr.hhplus.be.server.domain.entity.PointHistory;
import kr.hhplus.be.server.domain.repository.PointRepository;
import kr.hhplus.be.server.domain.repository.PointHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PointService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public PointService(PointRepository pointRepository,
                        PointHistoryRepository pointHistoryRepository) {
        this.pointRepository = pointRepository;
        this.pointHistoryRepository = pointHistoryRepository;
    }

    /**
     * 포인트 충전
     */
    @Transactional
    public Point chargePoint(Long userId, BigDecimal amount, String description) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }

        // 동시성 제어를 위한 비관적 락 사용
        Point point = pointRepository.findByUserIdWithLock(userId)
                .orElseGet(() -> {
                    Point newPoint = Point.create(userId);
                    return pointRepository.save(newPoint);
                });

        point.charge(amount);
        Point savedPoint = pointRepository.save(point);

        // 포인트 충전 이력 저장
        PointHistory history = PointHistory.charge(userId, amount, description);
        pointHistoryRepository.save(history);

        return savedPoint;
    }

    /**
     * 포인트 사용
     */
    @Transactional
    public Point usePoint(Long userId, BigDecimal amount, String description) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("사용 금액은 0보다 커야 합니다.");
        }

        Point point = pointRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("포인트 정보를 찾을 수 없습니다."));

        point.use(amount);
        Point savedPoint = pointRepository.save(point);

        // 포인트 사용 이력 저장
        PointHistory history = PointHistory.use(userId, amount, description);
        pointHistoryRepository.save(history);

        return savedPoint;
    }

    /**
     * 포인트 잔액 조회
     */
    public Point getPointBalance(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        return pointRepository.findByUserId(userId)
                .orElseGet(() -> Point.create(userId));
    }

    /**
     * 포인트 사용 가능 여부 확인
     */
    public boolean canUsePoints(Long userId, BigDecimal amount) {
        Point point = getPointBalance(userId);
        return point.hasEnoughBalance(amount);
    }

    /**
     * 포인트 이력 조회
     */
    public List<PointHistory> getPointHistory(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        return pointHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}

