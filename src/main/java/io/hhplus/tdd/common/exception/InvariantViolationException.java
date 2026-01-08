package io.hhplus.tdd.common.exception;

public class InvariantViolationException extends RuntimeException {

    public InvariantViolationException() {
        super();
    }

    public InvariantViolationException(String message) {
        super(message);
    }

    public InvariantViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
