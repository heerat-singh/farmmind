package com.farmmind.apigateway.dto;

import com.farmmind.apigateway.model.Farm;
import com.farmmind.apigateway.model.FarmCrop;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record FarmDto(
    UUID id,
    String name,
    BigDecimal latitude,
    BigDecimal longitude,
    String province,
    String climateZone,
    BigDecimal acreage,
    String farmType,
    List<CropDto> crops
) {
    public record CropDto(
        UUID id,
        String cropName,
        String variety,
        LocalDate plantingDate,
        LocalDate expectedHarvestDate,
        BigDecimal acreagePlanted,
        boolean active
    ) {
        public static CropDto from(FarmCrop c) {
            return new CropDto(c.getId(), c.getCropName(), c.getVariety(),
                    c.getPlantingDate(), c.getExpectedHarvestDate(), c.getAcreagePlanted(), c.isActive());
        }
    }

    public static FarmDto from(Farm farm) {
        return new FarmDto(
                farm.getId(),
                farm.getName(),
                farm.getLatitude(),
                farm.getLongitude(),
                farm.getProvince(),
                farm.getClimateZone(),
                farm.getAcreage(),
                farm.getFarmType(),
                farm.getCrops().stream()
                        .filter(FarmCrop::isActive)
                        .map(CropDto::from)
                        .toList()
        );
    }
}
