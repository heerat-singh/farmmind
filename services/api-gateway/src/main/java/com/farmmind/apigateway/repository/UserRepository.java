package com.farmmind.apigateway.repository;

import com.farmmind.apigateway.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByCognitoId(String cognitoId);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.questionsThisMonth = u.questionsThisMonth + 1 WHERE u.id = :id")
    void incrementQuestionCount(UUID id);

    @Modifying
    @Query("UPDATE User u SET u.voiceThisMonth = u.voiceThisMonth + 1 WHERE u.id = :id")
    void incrementVoiceCount(UUID id);

    @Modifying
    @Query("UPDATE User u SET u.photosThisMonth = u.photosThisMonth + 1 WHERE u.id = :id")
    void incrementPhotoCount(UUID id);
}
