package com.example.ledger.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateTransactionRequest(
        @Schema(example = "user-1")
        @NotBlank String userId,
        @Schema(example = "1200.50")
        @NotNull @Positive BigDecimal amount,
        @Schema(example = "Amazon")
        @NotBlank String merchant,
        @Schema(example = "2026-04-23T15:20:00Z")
        Instant timestamp
) {
}
