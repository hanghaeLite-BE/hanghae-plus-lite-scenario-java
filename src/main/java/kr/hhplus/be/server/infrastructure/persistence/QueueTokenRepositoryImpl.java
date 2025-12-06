package kr.hhplus.be.server.infrastructure.persistence;

import kr.hhplus.be.server.domain.model.QueueToken;
import kr.hhplus.be.server.domain.model.QueueStatus;
import kr.hhplus.be.server.domain.repository.QueueTokenRepository;
import kr.hhplus.be.server.infrastructure.persistence.entity.QueueTokenEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Transactional(readOnly = true)
public class QueueTokenRepositoryImpl implements QueueTokenRepository {

    private final QueueTokenJpaRepository jpaRepository;

    public QueueTokenRepositoryImpl(QueueTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public QueueToken save(QueueToken token) {
        QueueTokenEntity entity = toEntity(token);
        QueueTokenEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<QueueToken> findById(Long id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Optional<QueueToken> findByTokenValue(String tokenValue) {
        return jpaRepository.findByTokenValue(tokenValue)
                .map(this::toDomain);
    }

    @Override
    public Optional<QueueToken> findByUserIdAndConcertId(Long userId, Long concertId) {
        return jpaRepository.findByUserIdAndConcertId(userId, concertId)
                .map(this::toDomain);
    }

    // 대기열 관리
    @Override
    public List<QueueToken> findWaitingTokensByConcertId(Long concertId) {
        return jpaRepository.findByConcertIdAndStatusOrderByCreatedAtAsc(concertId, QueueStatus.WAITING)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<QueueToken> findActiveTokensByConcertId(Long concertId) {
        return jpaRepository.findByConcertIdAndStatus(concertId, QueueStatus.ACTIVE)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Integer getWaitingPosition(Long concertId, String tokenValue) {
        return jpaRepository.getWaitingPosition(concertId, tokenValue);
    }

    // 상태별 조회
    @Override
    public List<QueueToken> findByStatus(QueueStatus status) {
        return jpaRepository.findByStatus(status)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<QueueToken> findExpiredTokens(LocalDateTime currentTime) {
        return jpaRepository.findByExpiresAtBeforeAndStatusIn(
                        currentTime,
                        List.of(QueueStatus.WAITING, QueueStatus.ACTIVE)
                ).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    // 토큰 활성화 관리
    @Override
    public List<QueueToken> findTokensReadyToActivate(Long concertId, int limit) {
        return jpaRepository.findTokensReadyToActivate(concertId, limit)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Long countActiveTokensByConcertId(Long concertId) {
        return jpaRepository.countByConcertIdAndStatus(concertId, QueueStatus.ACTIVE);
    }

    @Override
    public Long countWaitingTokensByConcertId(Long concertId) {
        return jpaRepository.countByConcertIdAndStatus(concertId, QueueStatus.WAITING);
    }

    // 배치 처리용
    @Override
    @Transactional
    public void deleteExpiredTokens(LocalDateTime beforeDate) {
        jpaRepository.deleteByExpiresAtBeforeAndStatus(beforeDate, QueueStatus.EXPIRED);
    }

    @Override
    @Transactional
    public void updateTokenPositions(Long concertId) {
        jpaRepository.updateTokenPositions(concertId);
    }

    // Entity ↔ Domain 변환
    private QueueTokenEntity toEntity(QueueToken domain) {
        QueueTokenEntity entity = new QueueTokenEntity(
                domain.getTokenValue(),
                domain.getUserId(),
                domain.getConcertId(),
                domain.getStatus(),
                domain.getCreatedAt(),
                domain.getExpiresAt(),
                domain.getPosition(),
                domain.getEnteredAt()
        );

        // ID가 있는 경우 (업데이트)
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }

        return entity;
    }

    private QueueToken toDomain(QueueTokenEntity entity) {
        QueueToken domain = QueueToken.createWaitingToken(
                entity.getUserId(),
                entity.getConcertId()
        );

        // ID 할당 (리플렉션 또는 별도 메서드 사용)
        domain.assignId(entity.getId());
        domain.assignPosition(entity.getPosition());

        return domain;
    }
}
