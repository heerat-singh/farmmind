package com.farmmind.apigateway.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cognito_id", nullable = false, unique = true)
    private String cognitoId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    private String phone;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String language = "en";

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String role = "farmer";

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "subscription_tier", nullable = false, length = 20)
    @Builder.Default
    private String subscriptionTier = "free";

    @Column(name = "subscription_expires_at")
    private Instant subscriptionExpiresAt;

    @Column(name = "questions_this_month", nullable = false)
    @Builder.Default
    private int questionsThisMonth = 0;

    @Column(name = "voice_this_month", nullable = false)
    @Builder.Default
    private int voiceThisMonth = 0;

    @Column(name = "photos_this_month", nullable = false)
    @Builder.Default
    private int photosThisMonth = 0;

    @Column(name = "usage_reset_at", nullable = false)
    private Instant usageResetAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public boolean isFreeTier() {
        return "free".equals(subscriptionTier);
    }

    public boolean isPaidTier() {
        return !isFreeTier();
    }
}
