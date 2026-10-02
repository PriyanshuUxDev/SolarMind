package com.solarmind.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record AssessmentSummary(
    Long id,
    String locationLabel,
    String selectedPanelBrand,
    String selectedPanelModel,
    BigDecimal recommendedCapacityKw,
    BigDecimal actualCapacityKw,
    BigDecimal annualSavings,
    BigDecimal estimatedCost,
    BigDecimal paybackYears,
    boolean roofFeasible,
    boolean withinBudget,
    Instant createdAt) {}
