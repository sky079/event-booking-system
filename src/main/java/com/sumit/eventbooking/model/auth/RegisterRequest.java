package com.sumit.eventbooking.model.auth;

import com.sumit.eventbooking.model.user.Role;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "must be at least 8 characters") String password,
        @NotNull Role role) {}