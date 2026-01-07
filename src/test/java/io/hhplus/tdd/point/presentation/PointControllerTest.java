package io.hhplus.tdd.point.presentation;

import static org.assertj.core.api.Assertions.*;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.validation.ConstraintViolationException;

@SpringBootTest
class PointControllerTest {

	@Autowired
	private PointController pointController;

	// 테스트 작성 이유: 메서드 수준 Bean Validation(@PositiveOrZero)이 정상적으로 동작하는지 검증하기 위해
	@Test
	@DisplayName("회원 아이디가 음수로 주어졌을 때 포인트를 조회하면 ConstraintViolationException이 발생한다.")
	void givenNegativeId_whenFindPoint_thenThrowConstraintViolationException() {
		// given
		long zeroId = 0L;
		long negativeId = -1L;

		// when
		ThrowableAssert.ThrowingCallable zeroCase = () -> pointController.point(zeroId);
		ThrowableAssert.ThrowingCallable negativeCase = () -> pointController.point(negativeId);

		// then
		assertThatCode(zeroCase).doesNotThrowAnyException();
		assertThatThrownBy(negativeCase).isInstanceOf(ConstraintViolationException.class);
	}

	// 테스트 작성 이유: 메서드 수준 Bean Validation(@PositiveOrZero)이 정상적으로 동작하는지 검증하기 위해
	@Test
	@DisplayName("회원 아이디가 음수로 주어졌을 때 포인트 내역을 조회하면 ConstraintViolationException이 발생한다.")
	void givenNegativeId_whenFindPointHistory_thenThrowConstraintViolationException() {
		// given
		long zeroUserId = 0L;
		long negativeUserId = -1L;
        int page = 0;
        int size = 10;

		// when
		ThrowableAssert.ThrowingCallable zeroCase = () -> pointController.history(zeroUserId, page, size);
		ThrowableAssert.ThrowingCallable negativeCase = () -> pointController.history(negativeUserId, page, size);

		// then
		assertThatCode(zeroCase).doesNotThrowAnyException();
		assertThatThrownBy(negativeCase).isInstanceOf(ConstraintViolationException.class);
	}

    // 테스트 작성 이유: 페이지네이션 요청 객체 PageRequest가 정상적으로 동작하는지 검증하기 위해
    @Test
    @DisplayName("PageRequest가 경계값으로 주어졌을 때 포인트 내역을 조회하면 IllegalArgumentException이 발생한다.")
    void givenInvalidPagingParams_whenFindPointHistory_thenThrowIllegalArgumentException() {
        // given
        long userId = 0L;

        int normalPage = 0;
        int invalidSize = 0;

        int invalidPage = -1;
        int normalSize = 10;

        // when
        ThrowableAssert.ThrowingCallable invalidSizeCase = () -> pointController.history(userId, normalPage, invalidSize);
        ThrowableAssert.ThrowingCallable invalidPageCase = () -> pointController.history(userId, invalidPage, normalSize);

        // then
        assertThatThrownBy(invalidSizeCase)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("size 값은 1 이상이어야 합니다.");
        assertThatThrownBy(invalidPageCase)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("page 값은 0 이상이어야 합니다.");
    }

    // 테스트 작성 이유: 메서드 수준 Bean Validation(@PositiveOrZero)이 정상적으로 동작하는지 검증하기 위해
	@Test
	@DisplayName("회원 아이디가 음수로 주어졌을 때 포인트를 충전하면 ConstraintViolationException이 발생한다.")
	void givenNegativeId_whenChargePoint_thenThrowConstraintViolationException() {
		// given
		long zeroUserId = 0L;
		long negativeUserId = -1L;
        long amount = 100L;

		// when
		ThrowableAssert.ThrowingCallable zeroCase = () -> pointController.charge(zeroUserId, amount);
		ThrowableAssert.ThrowingCallable negativeCase = () -> pointController.charge(negativeUserId, amount);

		// then
		assertThatCode(zeroCase).doesNotThrowAnyException();
		assertThatThrownBy(negativeCase).isInstanceOf(ConstraintViolationException.class);
	}

    // 테스트 작성 이유: 메서드 수준 Bean Validation(@PositiveOrZero)이 정상적으로 동작하는지 검증하기 위해
	@Test
	@DisplayName("충전 금액으로 0 이하의 값이 주어졌을 때 포인트를 충전하면 ConstraintViolationException이 발생한다.")
	void givenLEZeroAmount_whenChargePoint_thenThrowConstraintViolationException() {
		// given
		long userId = 0L;
        long normalAmount = 100L;
		long zeroAmount = 0L;
        long negativeAmount = -1L;

		// when
        ThrowableAssert.ThrowingCallable normalCase = () -> pointController.charge(userId, normalAmount);
		ThrowableAssert.ThrowingCallable zeroCase = () -> pointController.charge(userId, zeroAmount);
		ThrowableAssert.ThrowingCallable negativeCase = () -> pointController.charge(userId, negativeAmount);

		// then
        assertThatCode(normalCase).doesNotThrowAnyException();
        assertThatThrownBy(zeroCase).isInstanceOf(ConstraintViolationException.class);
		assertThatThrownBy(negativeCase).isInstanceOf(ConstraintViolationException.class);
	}

    // 테스트 작성 이유: 메서드 수준 Bean Validation(@PositiveOrZero)이 정상적으로 동작하는지 검증하기 위해
	@Test
	@DisplayName("사용 금액으로 0 이하의 값이 주어졌을 때 포인트를 사용하면 ConstraintViolationException이 발생한다.")
	void givenLEZeroAmount_whenUsePoint_thenThrowConstraintViolationException() {
		// given
		long userId = 0L;
        long normalAmount = 100L;
		long zeroAmount = 0L;
        long negativeAmount = -1L;

		pointController.charge(userId, normalAmount);

		// when
        ThrowableAssert.ThrowingCallable normalCase = () -> pointController.use(userId, normalAmount);
		ThrowableAssert.ThrowingCallable zeroCase = () -> pointController.use(userId, zeroAmount);
		ThrowableAssert.ThrowingCallable negativeCase = () -> pointController.use(userId, negativeAmount);

		// then
        assertThatCode(normalCase).doesNotThrowAnyException();
        assertThatThrownBy(zeroCase).isInstanceOf(ConstraintViolationException.class);
		assertThatThrownBy(negativeCase).isInstanceOf(ConstraintViolationException.class);
	}
}
