package com.innowise.userservice.dto.response;

import lombok.Builder;

@Builder
public record UserResponse(

        Long id,

        String name,

        String surname,

        String email,

        Boolean active

) {}
