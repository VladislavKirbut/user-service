package com.innowise.userservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Builder
public record UpdateUserRequest(

        @NotBlank(message = "User's name is required")
        @Size(min = 1, max = 100, message = "User's name must be between 1 and 100 characters")
        String name,

        @NotBlank(message = "User's surname is required")
        @Size(min = 1, max = 100, message = "User's surname must be between 1 and 100 characters")
        String surname,

        @NotNull(message = "Birth date is required")
        @Past(message = "Birth date must be a date in the past")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate birthDate,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email

) {}