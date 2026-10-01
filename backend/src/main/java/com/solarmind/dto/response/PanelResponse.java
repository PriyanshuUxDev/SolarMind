package com.solarmind.dto.response;

import java.math.BigDecimal;

public record PanelResponse(
    Long id,
    String brand,
    String model,
    Integer wattage,
    BigDecimal dailyOutput,
    BigDecimal monthlyOutput,
    BigDecimal efficiency,
    BigDecimal vmpp,
    BigDecimal impp,
    String dimensions,
    BigDecimal weight) {}
