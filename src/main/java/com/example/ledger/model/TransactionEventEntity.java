package com.example.ledger.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "transaction_events",
        indexes = {
                @Index(name = "idx_transaction_events_transaction_id", columnList = "transaction_id")
        }
)
@Immutable
public class TransactionEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false, updatable = false)
    private UUID transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, updatable = false, length = 32)
    private EventType eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, updatable = false)
    private TransactionEvent payload;

    @Column(name = "ai_decision", updatable = false)
    private String aiDecision;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected TransactionEventEntity() {
    }

    private TransactionEventEntity(
            UUID transactionId,
            EventType eventType,
            TransactionEvent payload,
            String aiDecision,
            Instant createdAt
    ) {
        this.transactionId = transactionId;
        this.eventType = eventType;
        this.payload = payload;
        this.aiDecision = aiDecision;
        this.createdAt = createdAt;
    }

    public static TransactionEventEntity append(
            UUID transactionId,
            EventType eventType,
            TransactionEvent payload,
            String aiDecision
    ) {
        return new TransactionEventEntity(transactionId, eventType, payload, aiDecision, Instant.now());
    }

    public Long getId() {
        return id;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public TransactionEvent getPayload() {
        return payload;
    }

    public String getAiDecision() {
        return aiDecision;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
