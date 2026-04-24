package com.farmmind.apigateway.controller;

import com.farmmind.apigateway.dto.CreateFarmRequest;
import com.farmmind.apigateway.dto.FarmDto;
import com.farmmind.apigateway.service.FarmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/farms")
@RequiredArgsConstructor
public class FarmController {

    private final FarmService farmService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FarmDto create(@AuthenticationPrincipal Jwt jwt,
                          @Valid @RequestBody CreateFarmRequest req) {
        return farmService.createFarm(jwt.getSubject(), req);
    }

    @GetMapping
    public List<FarmDto> list(@AuthenticationPrincipal Jwt jwt) {
        return farmService.listFarms(jwt.getSubject());
    }

    @GetMapping("/{id}")
    public FarmDto get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return farmService.getFarm(jwt.getSubject(), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        farmService.deleteFarm(jwt.getSubject(), id);
    }
}
