package com.solarmind.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record AssessmentRequest(
    @NotNull Long locationId,
    @NotNull @Positive @DecimalMax(value = ValidationLimits.MAX_MONTHLY_CONSUMPTION) BigDecimal monthlyConsumption,
    @NotNull @PositiveOrZero @DecimalMax(value = ValidationLimits.MAX_MONTHLY_BILL) BigDecimal monthlyBill,
    @NotNull @Positive @DecimalMax(value = ValidationLimits.MAX_ROOF_AREA) BigDecimal roofArea,
    @NotNull RoofType roofType,
    @NotNull @Positive @DecimalMax(value = ValidationLimits.MAX_BUDGET) BigDecimal budget) {}
