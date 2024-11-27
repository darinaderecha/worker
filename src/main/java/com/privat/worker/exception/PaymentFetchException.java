package com.privat.worker.exception;

public class PaymentFetchException extends RuntimeException {
    public PaymentFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
