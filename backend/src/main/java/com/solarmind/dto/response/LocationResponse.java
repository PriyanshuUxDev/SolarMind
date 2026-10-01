package com.solarmind.dto.response;

public record LocationResponse(
    Long id,
    String region,
    String city,
    String district,
    String state,
    Double latitude,
    Double longitude) {}
