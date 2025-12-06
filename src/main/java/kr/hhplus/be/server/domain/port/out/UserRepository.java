package kr.hhplus.be.server.domain.port.out;

import kr.hhplus.be.server.domain.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long userId);
    User save(User user);

    @Transactional(readOnly = true)
    Optional<User> findByUserId(String userId);

    @Transactional
    Optional<User> findByIdWithLock(Long id);

    @Transactional
    Optional<User> findByUserIdWithLock(String userId);
}
