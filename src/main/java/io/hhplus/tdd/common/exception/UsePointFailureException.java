package io.hhplus.tdd.common.exception;

public class UsePointFailureException extends RuntimeException {

    public UsePointFailureException() {
        super();
    }

    public UsePointFailureException(String message) {
        super(message);
    }

    public UsePointFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
