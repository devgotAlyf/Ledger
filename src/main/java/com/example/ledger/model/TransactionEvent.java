package com.example.ledger.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionEvent(
        @Schema(example = "123e4567-e89b-12d3-a456-426614174000")
        UUID transactionId,
        @Schema(example = "user-1")
        String userId,
        @Schema(example = "1200.50")
        BigDecimal amount,
        @Schema(example = "Amazon")
        String merchant,
        @Schema(example = "2026-04-23T15:20:00Z")
        Instant timestamp,
        @Schema(example = "APPROVED")
        String status
) {
}
