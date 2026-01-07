package io.hhplus.tdd.common;

import io.hhplus.tdd.common.exception.AmountExceedBalanceException;
import io.hhplus.tdd.common.exception.ChargePointFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.hhplus.tdd.common.exception.UsePointFailureException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {

	@ExceptionHandler(value = ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
		return ResponseEntity.status(400).body(new ErrorResponse("400", "잘못된 요청 파라미터입니다."));
	}

	@ExceptionHandler(value = {AmountExceedBalanceException.class, IllegalArgumentException.class})
	public ResponseEntity<ErrorResponse> handleAmountExceedBalanceException(RuntimeException e) {
		return ResponseEntity.status(400).body(new ErrorResponse("400", e.getMessage()));
	}

    @ExceptionHandler(value = {ChargePointFailureException.class, UsePointFailureException.class})
    public ResponseEntity<ErrorResponse> handleChargePointFailureException(RuntimeException e) {
        return ResponseEntity.status(500).body(new ErrorResponse("500", e.getMessage()));
    }

	@ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(500).body(new ErrorResponse("500", "에러가 발생했습니다."));
    }
}
