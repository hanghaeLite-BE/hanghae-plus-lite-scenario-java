package kr.hhplus.be.server.infrastructure.persistence;

import kr.hhplus.be.server.domain.model.ReservationToken;
import kr.hhplus.be.server.domain.model.TokenStatus;
import kr.hhplus.be.server.domain.repository.ReservationTokenRepository;
import kr.hhplus.be.server.infrastructure.persistence.entity.ReservationTokenEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationTokenRepositoryImpl implements ReservationTokenRepository {

    private final ReservationTokenJpaRepository jpaRepository;

    public ReservationTokenRepositoryImpl(ReservationTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Transactional
    @Override
    public ReservationToken save(ReservationToken token) {
        ReservationTokenEntity entity = toEntity(token);
        ReservationTokenEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ReservationToken> findByToken(String token) {
        return jpaRepository.findByToken(token)
                .map(this::toDomain);
    }

    @Override
    public Optional<ReservationToken> findByUserId(Long userId) {
        List<TokenStatus> activeStatuses = List.of(TokenStatus.WAITING, TokenStatus.ACTIVE);
        return jpaRepository.findByUserIdAndStatusIn(userId, activeStatuses)
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public void deleteByToken(String token) {
        jpaRepository.findByToken(token)
                .ifPresent(jpaRepository::delete);
    }

    @Override
    public long countActiveTokens() {
        return jpaRepository.countActiveTokens(LocalDateTime.now());
    }

    @Override
    public long getWaitingPosition(String token) {
        Optional<ReservationTokenEntity> tokenEntity = jpaRepository.findByToken(token);
        if (tokenEntity.isEmpty() || tokenEntity.get().getWaitingNumber() == null) {
            return 0;
        }

        return jpaRepository.countWaitingTokensBeforeNumber(tokenEntity.get().getWaitingNumber()) + 1;
    }

    @Override
    @Transactional
    public void activateNextTokens(int count) {
        List<ReservationTokenEntity> waitingTokens = jpaRepository
                .findWaitingTokensOrderByCreatedAt(LocalDateTime.now());

        // 활성화할 토큰 개수만큼 처리
        waitingTokens.stream()
                .limit(count)
                .forEach(token -> {
                    token.setStatus(TokenStatus.ACTIVE);
                    jpaRepository.save(token);
                });
    }

    @Override
    @Transactional
    public void deleteExpiredTokens() {
        jpaRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    // 다음 대기 번호 생성
    public Long generateNextWaitingNumber() {
        Long maxNumber = jpaRepository.findMaxWaitingNumber();
        return maxNumber + 1;
    }

    // Entity → Domain 변환
    private ReservationToken toDomain(ReservationTokenEntity entity) {
        ReservationToken token;

        if (entity.getStatus() == TokenStatus.WAITING) {
            token = ReservationToken.createWaitingToken(
                    entity.getToken(),
                    entity.getUserId(),
                    entity.getWaitingNumber()
            );
        } else {
            token = ReservationToken.createActiveToken(
                    entity.getToken(),
                    entity.getUserId()
            );
        }

        // ID 할당
        token.assignId(entity.getId());
        return token;
    }

    // Domain → Entity 변환
    private ReservationTokenEntity toEntity(ReservationToken domain) {
        return new ReservationTokenEntity(
                domain.getToken(),
                domain.getUserId(),
                domain.getStatus(),
                domain.getCreatedAt(),
                domain.getExpiresAt(),
                domain.getWaitingNumber()
        );
    }
}