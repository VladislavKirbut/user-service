package com.innowise.userservice.dto.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record PaymentCardResponse(

        Long id,

        String number,

        String holder,

        LocalDate expirationDate,

        Boolean active

) {}
