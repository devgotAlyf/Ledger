package com.example.ledger.model;

public record AiAuditDecision(
        EventType eventType,
        String aiDecision,
        String reason
) {
}
