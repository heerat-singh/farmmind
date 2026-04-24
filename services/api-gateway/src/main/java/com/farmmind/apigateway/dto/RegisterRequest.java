package com.farmmind.apigateway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank String fullName,

    @NotBlank @Email String email,

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one uppercase letter")
    @Pattern(regexp = ".*[0-9].*", message = "Password must contain at least one number")
    String password,

    @Pattern(regexp = "en|pa|hi|fr", message = "Language must be en, pa, hi, or fr")
    String language
) {
    public String language() {
        return language != null ? language : "en";
    }
}
