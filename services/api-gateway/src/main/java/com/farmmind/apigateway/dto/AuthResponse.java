package com.farmmind.apigateway.dto;

import com.farmmind.apigateway.model.User;

import java.util.UUID;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    UserDto user
) {
    public record UserDto(
        UUID id,
        String email,
        String fullName,
        String language,
        String subscriptionTier,
        int questionsThisMonth,
        boolean onboarded
    ) {
        public static UserDto from(User user, boolean onboarded) {
            return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getLanguage(),
                user.getSubscriptionTier(),
                user.getQuestionsThisMonth(),
                onboarded
            );
        }
    }
}
