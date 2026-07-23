package com.innowise.userservice.exception;

public class PaymentCardNotFoundException extends RuntimeException {

    public PaymentCardNotFoundException(Long id) {
        super("Payment card with id: %s not found".formatted(id));
    }

}
