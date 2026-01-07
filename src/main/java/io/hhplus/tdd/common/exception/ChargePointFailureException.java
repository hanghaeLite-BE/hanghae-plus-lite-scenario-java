package io.hhplus.tdd.common.exception;

public class ChargePointFailureException extends RuntimeException {

    public ChargePointFailureException() {
        super();
    }

    public ChargePointFailureException(String message) {
        super(message);
    }

    public ChargePointFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
