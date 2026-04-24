package com.example.ledger.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record TransactionAcceptedResponse(
        @Schema(example = "123e4567-e89b-12d3-a456-426614174000")
        UUID transactionId,
        @Schema(example = "CREATED")
        String status
) {
}
