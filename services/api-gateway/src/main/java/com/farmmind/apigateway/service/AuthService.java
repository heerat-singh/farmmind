package com.farmmind.apigateway.service;

import com.farmmind.apigateway.dto.AuthResponse;
import com.farmmind.apigateway.dto.LoginRequest;
import com.farmmind.apigateway.dto.RegisterRequest;
import com.farmmind.apigateway.exception.ResourceNotFoundException;
import com.farmmind.apigateway.model.User;
import com.farmmind.apigateway.repository.FarmRepository;
import com.farmmind.apigateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final CognitoIdentityProviderClient cognitoClient;
    private final UserRepository userRepository;
    private final FarmRepository farmRepository;

    @Value("${app.cognito.user-pool-id}")
    private String userPoolId;

    @Value("${app.cognito.client-id}")
    private String clientId;

    @Value("${app.cognito.client-secret:}")
    private String clientSecret;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        // Sign up in Cognito
        var signUpBuilder = SignUpRequest.builder()
                .clientId(clientId)
                .username(req.email())
                .password(req.password())
                .userAttributes(
                        AttributeType.builder().name("email").value(req.email()).build(),
                        AttributeType.builder().name("name").value(req.fullName()).build()
                );

        if (!clientSecret.isBlank()) {
            signUpBuilder.secretHash(computeSecretHash(req.email()));
        }

        SignUpResponse signUpResponse;
        try {
            signUpResponse = cognitoClient.signUp(signUpBuilder.build());
        } catch (UsernameExistsException e) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Persist user locally (mirrors Cognito)
        User user = User.builder()
                .cognitoId(signUpResponse.userSub())
                .email(req.email())
                .fullName(req.fullName())
                .language(req.language())
                .usageResetAt(firstOfNextMonth())
                .build();
        userRepository.save(user);

        // Auto-confirm + sign in (dev/test: admin confirm)
        adminConfirmAndLogin(req.email(), req.password());

        var tokens = initiateAuth(req.email(), req.password());
        return buildResponse(tokens, user, false);
    }

    public AuthResponse login(LoginRequest req) {
        var tokens = initiateAuth(req.email(), req.password());

        // Sync user from Cognito sub claim if first login on new device
        String sub = extractSubFromToken(tokens.get("IdToken"));
        User user = userRepository.findByCognitoId(sub)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean onboarded = farmRepository.findByOwnerIdAndActiveTrue(user.getId()).size() > 0;
        return buildResponse(tokens, user, onboarded);
    }

    private Map<String, String> initiateAuth(String email, String password) {
        var authBuilder = InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .clientId(clientId)
                .authParameters(Map.of(
                        "USERNAME", email,
                        "PASSWORD", password
                ));

        if (!clientSecret.isBlank()) {
            authBuilder.authParameters(Map.of(
                    "USERNAME", email,
                    "PASSWORD", password,
                    "SECRET_HASH", computeSecretHash(email)
            ));
        }

        try {
            InitiateAuthResponse res = cognitoClient.initiateAuth(authBuilder.build());
            var result = res.authenticationResult();
            return Map.of(
                    "AccessToken", result.accessToken(),
                    "IdToken", result.idToken(),
                    "RefreshToken", result.refreshToken()
            );
        } catch (NotAuthorizedException e) {
            throw new IllegalArgumentException("Invalid email or password");
        } catch (UserNotConfirmedException e) {
            throw new IllegalArgumentException("Email not verified. Check your inbox.");
        }
    }

    private void adminConfirmAndLogin(String email, String password) {
        try {
            cognitoClient.adminConfirmSignUp(
                    AdminConfirmSignUpRequest.builder()
                            .userPoolId(userPoolId)
                            .username(email)
                            .build()
            );
        } catch (Exception e) {
            // Already confirmed or confirmation not needed — proceed
            log.debug("adminConfirmSignUp skipped: {}", e.getMessage());
        }
    }

    private AuthResponse buildResponse(Map<String, String> tokens, User user, boolean onboarded) {
        return new AuthResponse(
                tokens.get("AccessToken"),
                tokens.get("RefreshToken"),
                AuthResponse.UserDto.from(user, onboarded)
        );
    }

    private String extractSubFromToken(String idToken) {
        // JWT payload is base64-encoded middle segment
        String[] parts = idToken.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        // Simple extraction — in production Spring Security handles this via JWT filter
        int start = payload.indexOf("\"sub\":\"") + 7;
        int end = payload.indexOf("\"", start);
        return payload.substring(start, end);
    }

    private String computeSecretHash(String username) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(clientSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            mac.update(username.getBytes(StandardCharsets.UTF_8));
            mac.update(clientId.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(mac.doFinal());
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute Cognito secret hash", e);
        }
    }

    private Instant firstOfNextMonth() {
        return Instant.now().truncatedTo(ChronoUnit.DAYS)
                .atZone(java.time.ZoneOffset.UTC)
                .withDayOfMonth(1)
                .plusMonths(1)
                .toInstant();
    }
}
