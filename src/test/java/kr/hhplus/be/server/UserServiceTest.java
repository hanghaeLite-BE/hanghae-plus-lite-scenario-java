package kr.hhplus.be.server;

import kr.hhplus.be.server.application.UserService;
import kr.hhplus.be.server.domain.model.User;
import kr.hhplus.be.server.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository);
    }

    @Test
    @DisplayName("사용자 잔액 조회 - 성공")
    void getUserBalance_Success() {
        // Given
        String userId = "user123";
        User user = User.create(userId, 10000L);
        user.assignId(1L);

        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        // When
        User result = userService.getUserBalance(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(10000L, result.getBalance());

        verify(userRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("사용자 잔액 충전 - 성공")
    void chargeBalance_Success() {
        // Given
        String userId = "user123";
        Long chargeAmount = 5000L;
        User originalUser = User.create(userId, 10000L);
        originalUser.assignId(1L);

        User chargedUser = originalUser.chargeBalance(chargeAmount);

        when(userRepository.findByUserIdWithLock(userId)).thenReturn(Optional.of(originalUser));
        when(userRepository.save(any(User.class))).thenReturn(chargedUser);

        // When
        User result = userService.chargeBalance(userId, chargeAmount);

        // Then
        assertNotNull(result);
        assertEquals(15000L, result.getBalance());

        verify(userRepository).findByUserIdWithLock(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 생성 - 성공")
    void createUser_Success() {
        // Given
        String userId = "newuser";
        Long initialBalance = 0L;
        User createdUser = User.create(userId, initialBalance);
        createdUser.assignId(1L);

        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(createdUser);

        // When
        User result = userService.createUser(userId, initialBalance);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(initialBalance, result.getBalance());

        verify(userRepository).findByUserId(userId);
        verify(userRepository).save(any(User.class));
    }
}