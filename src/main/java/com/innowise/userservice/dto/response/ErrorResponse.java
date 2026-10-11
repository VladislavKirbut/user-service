package com.innowise.userservice.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(

        int status,

        String error,

        LocalDateTime timestamp,

        String message,

        String path

) {}
