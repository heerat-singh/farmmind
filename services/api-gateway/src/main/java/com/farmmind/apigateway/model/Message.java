package com.farmmind.apigateway.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(nullable = false, length = 10)
    private String role; // 'user' or 'assistant'

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_s3_key", length = 500)
    private String imageS3Key;

    @Column(name = "voice_s3_key", length = 500)
    private String voiceS3Key;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @Column(name = "processing_ms")
    private Integer processingMs;

    @Column
    private Short feedback; // 1 = thumbs up, -1 = thumbs down

    @Column(name = "feedback_reason", length = 100)
    private String feedbackReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
