package com.innowise.userservice.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record UserDetailsResponse(

        Long id,

        String name,

        String surname,

        LocalDate birthDate,

        String email,

        Boolean active,

        List<PaymentCardResponse> paymentCards

) {}
