package com.farmmind.apigateway.repository;

import com.farmmind.apigateway.model.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    Page<Conversation> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Optional<Conversation> findByIdAndUserId(UUID id, UUID userId);
}
