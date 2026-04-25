package com.example.ledger.kafka;

import com.example.ledger.config.KafkaNotConfiguredCondition;
import com.example.ledger.model.TransactionEvent;
import com.example.ledger.service.TransactionAuditService;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Conditional(KafkaNotConfiguredCondition.class)
public class LocalTransactionPublisher implements TransactionEventPublisher {

    private final TransactionAuditService transactionAuditService;

    public LocalTransactionPublisher(TransactionAuditService transactionAuditService) {
        this.transactionAuditService = transactionAuditService;
    }

    @Override
    public CompletableFuture<Void> publish(TransactionEvent event) {
        CompletableFuture.runAsync(() -> transactionAuditService.audit(event));
        return CompletableFuture.completedFuture(null);
    }
}
