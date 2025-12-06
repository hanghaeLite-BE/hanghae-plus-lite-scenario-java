package kr.hhplus.be.server.application;

import kr.hhplus.be.server.domain.model.User;
import kr.hhplus.be.server.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User getUserBalance(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
    }

    @Transactional
    public User chargeBalance(String userId, Long amount) {
        // 비관적 락으로 동시성 제어
        User user = userRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        User chargedUser = user.chargeBalance(amount);
        return userRepository.save(chargedUser);
    }

    @Transactional
    public User createUser(String userId, Long initialBalance) {
        // 중복 체크
        if (userRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 사용자입니다: " + userId);
        }

        User user = User.create(userId, initialBalance != null ? initialBalance : 0L);
        return userRepository.save(user);
    }
}
