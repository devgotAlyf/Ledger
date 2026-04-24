package com.example.ledger.kafka;

import com.example.ledger.guard.IdempotencyGuard;
import com.example.ledger.model.TransactionEvent;
import com.example.ledger.service.TransactionAuditService;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Profile("!local")
public class TransactionAuditConsumer {

    private final IdempotencyGuard idempotencyGuard;
    private final TransactionAuditService transactionAuditService;

    public TransactionAuditConsumer(
            IdempotencyGuard idempotencyGuard,
            TransactionAuditService transactionAuditService
    ) {
        this.idempotencyGuard = idempotencyGuard;
        this.transactionAuditService = transactionAuditService;
    }

    @KafkaListener(topics = "${audit.kafka.transaction-topic}")
    public void consume(TransactionEvent event) {
        String key = "audit:transaction:" + event.transactionId() + ":" + event.status();

        if (!idempotencyGuard.tryAcquire(key)) {
            return;
        }

        try {
            transactionAuditService.audit(event);
            idempotencyGuard.markProcessed(key);
        } catch (RuntimeException exception) {
            idempotencyGuard.release(key);
            throw exception;
        }
    }
}
