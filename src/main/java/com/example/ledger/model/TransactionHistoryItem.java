package com.example.ledger.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record TransactionHistoryItem(
        @Schema(example = "1")
        Long id,
        @Schema(example = "123e4567-e89b-12d3-a456-426614174000")
        UUID transactionId,
        EventType eventType,
        TransactionEvent payload,
        @Schema(example = "APPROVED")
        String aiDecision,
        @Schema(example = "2026-04-23T15:20:01Z")
        Instant createdAt
) {
    public static TransactionHistoryItem from(TransactionEventEntity entity) {
        return new TransactionHistoryItem(
                entity.getId(),
                entity.getTransactionId(),
                entity.getEventType(),
                entity.getPayload(),
                entity.getAiDecision(),
                entity.getCreatedAt()
        );
    }
}
