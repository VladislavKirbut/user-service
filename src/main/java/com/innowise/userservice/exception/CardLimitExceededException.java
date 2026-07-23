package com.innowise.userservice.exception;

public class CardLimitExceededException extends RuntimeException {

    public CardLimitExceededException(Long userId) {
        super("User with id: %s already has the maximum number of payment cards".formatted(userId));
    }

}
