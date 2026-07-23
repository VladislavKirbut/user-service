package com.innowise.userservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Builder
public record CreatePaymentCardRequest(

        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "\\d{16}$", message = "Card number must contain exactly 16 digits")
        String number,

        @NotBlank(message = "Card holder is required")
        @Size(min = 1, max = 100, message = "Card holder must be between 1 and 100 characters")
        String holder,

        @NotNull(message = "Card expiration date is required")
        @Future(message = "Card expiration date must be in the future")
        @DateTimeFormat(pattern = "yyy-MM-dd")
        LocalDate expirationDate

) {}
