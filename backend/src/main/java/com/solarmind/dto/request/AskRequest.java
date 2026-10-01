package com.solarmind.dto.request;

import jakarta.validation.constraints.*;

public record AskRequest(@NotBlank @Size(max = 500) String question, Long assessmentId) {}
