package kr.hhplus.be.server.infrastructure.persistence;

import kr.hhplus.be.server.domain.model.User;
import kr.hhplus.be.server.domain.port.out.UserRepository;
import kr.hhplus.be.server.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryImpl(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    @Transactional
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity savedEntity = userJpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<User> findByUserId(String userId) {
        return userJpaRepository.findByUserId(userId)
                .map(this::toDomain);
    }

    @Transactional
    @Override
    public Optional<User> findByIdWithLock(Long id) {
        return userJpaRepository.findByIdWithLock(id)
                .map(this::toDomain);
    }

    @Transactional
    @Override
    public Optional<User> findByUserIdWithLock(String userId) {
        return userJpaRepository.findByUserIdWithLock(userId)
                .map(this::toDomain);
    }

    // Entity → Domain 변환
    private User toDomain(UserEntity entity) {
        User user = User.create(entity.getUserId(), entity.getBalance());
        user.assignId(entity.getId());
        return user;
    }

    // Domain → Entity 변환
    private UserEntity toEntity(User user) {
        if (user.getId() != null) {
            // 기존 엔티티 업데이트
            Optional<UserEntity> existingEntity = userJpaRepository.findById(user.getId());
            if (existingEntity.isPresent()) {
                UserEntity entity = existingEntity.get();
                entity.setUserId(user.getUserId());
                entity.setBalance(user.getBalance());
                return entity;
            }
        }

        // 새 엔티티 생성
        return new UserEntity(user.getUserId(), user.getBalance());
    }
}