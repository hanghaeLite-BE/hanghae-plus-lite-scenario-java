package io.hhplus.tdd.common.exception;

public class AmountExceedBalanceException extends RuntimeException {

    public AmountExceedBalanceException() {
        super();
    }

    public AmountExceedBalanceException(String message) {
        super(message);
    }

    public AmountExceedBalanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
