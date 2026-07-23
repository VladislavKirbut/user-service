package com.innowise.userservice.exception;

public class PaymentCardAlreadyExistsException extends RuntimeException {

    public PaymentCardAlreadyExistsException(String number) {
        super("Payment card with number %s already exists".formatted(number));
    }

}
