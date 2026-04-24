package com.farmmind.apigateway.service;

import com.farmmind.apigateway.dto.CreateFarmRequest;
import com.farmmind.apigateway.dto.FarmDto;
import com.farmmind.apigateway.exception.ResourceNotFoundException;
import com.farmmind.apigateway.model.Farm;
import com.farmmind.apigateway.model.FarmCrop;
import com.farmmind.apigateway.model.User;
import com.farmmind.apigateway.repository.FarmRepository;
import com.farmmind.apigateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FarmService {

    private final FarmRepository farmRepository;
    private final UserRepository userRepository;
    private final ClimateZoneService climateZoneService;

    @Transactional
    public FarmDto createFarm(String cognitoId, CreateFarmRequest req) {
        User owner = userRepository.findByCognitoId(cognitoId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String climateZone = climateZoneService.resolve(
                req.latitude().doubleValue(), req.longitude().doubleValue());

        Farm farm = Farm.builder()
                .owner(owner)
                .name(req.name())
                .latitude(req.latitude())
                .longitude(req.longitude())
                .province(req.province())
                .climateZone(climateZone)
                .acreage(req.acreage())
                .farmType(req.farmType())
                .build();

        if (req.crops() != null) {
            req.crops().forEach(c -> {
                FarmCrop crop = FarmCrop.builder()
                        .farm(farm)
                        .cropName(c.cropName())
                        .variety(c.variety())
                        .build();
                farm.getCrops().add(crop);
            });
        }

        return FarmDto.from(farmRepository.save(farm));
    }

    @Transactional(readOnly = true)
    public List<FarmDto> listFarms(String cognitoId) {
        User owner = userRepository.findByCognitoId(cognitoId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return farmRepository.findByOwnerIdAndActiveTrue(owner.getId())
                .stream().map(FarmDto::from).toList();
    }

    @Transactional(readOnly = true)
    public FarmDto getFarm(String cognitoId, UUID farmId) {
        User owner = userRepository.findByCognitoId(cognitoId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Farm farm = farmRepository.findByIdAndOwnerId(farmId, owner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found"));
        return FarmDto.from(farm);
    }

    @Transactional
    public void deleteFarm(String cognitoId, UUID farmId) {
        User owner = userRepository.findByCognitoId(cognitoId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Farm farm = farmRepository.findByIdAndOwnerId(farmId, owner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found"));
        farm.setActive(false); // soft delete
        farmRepository.save(farm);
    }
}
