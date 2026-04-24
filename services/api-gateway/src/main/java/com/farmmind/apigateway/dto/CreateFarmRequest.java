package com.farmmind.apigateway.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record CreateFarmRequest(
    @NotBlank String name,

    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")
    BigDecimal latitude,

    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
    BigDecimal longitude,

    String province,

    @Positive BigDecimal acreage,

    @Pattern(regexp = "vegetable|grain|orchard|greenhouse|mixed|other",
             message = "Invalid farm type")
    String farmType,

    @Valid List<CropRequest> crops
) {
    public record CropRequest(
        @NotBlank String cropName,
        String variety
    ) {}
}
