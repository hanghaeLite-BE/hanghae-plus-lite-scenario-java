package kr.hhplus.be.server.domain.port.out;

import kr.hhplus.be.server.domain.model.User;

public interface UserRepository {
    User findById(Long userId);
    User save(User user);
}
