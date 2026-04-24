package com.farmmind.apigateway.repository;

import com.farmmind.apigateway.model.Farm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FarmRepository extends JpaRepository<Farm, UUID> {

    List<Farm> findByOwnerIdAndActiveTrue(UUID ownerId);

    Optional<Farm> findByIdAndOwnerId(UUID id, UUID ownerId);
}
