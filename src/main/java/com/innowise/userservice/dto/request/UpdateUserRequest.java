package com.innowise.userservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Setter;

import java.time.LocalDate;

@Builder(toBuilder = true)
public record UpdateUserRequest(

        @NotBlank(message = "Name cannot be blank")
        @Size(max = 100, message = "Name length must not exceed 50 characters")
        String name,

        @NotBlank(message = "Surname cannot be blank")
        @Size(max = 100, message = "Surname length must not exceed 100 characters")
        String surname,

        @NotNull(message = "Birth date cannot be null")
        @Past(message = "Birth date must be in the past")
        LocalDate birthDate,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        @Size(max = 255, message = "Email length must not exceed 255 characters")
        String email

) {}