package com.innowise.userservice.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UpdatePaymentCardRequest(

        @NotBlank(message = "Card number cannot be blank")
        @Size(min = 13, max = 19)
        String number,

        @NotBlank(message = "Card holder cannot be blank")
        String holder,

        @NotNull(message = "Expiration date cannot be null")
        @Future
        LocalDate expirationDate

) {
}
