package com.example.ledger.kafka;

import com.example.ledger.config.KafkaConfiguredCondition;
import com.example.ledger.model.AuditProperties;
import com.example.ledger.model.TransactionEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Conditional;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Profile("!local")
@Conditional(KafkaConfiguredCondition.class)
public class TransactionProducer implements TransactionEventPublisher {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
    private final AuditProperties auditProperties;

    public TransactionProducer(
            KafkaTemplate<String, TransactionEvent> kafkaTemplate,
            AuditProperties auditProperties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.auditProperties = auditProperties;
    }

    @Override
    public CompletableFuture<Void> publish(TransactionEvent event) {
        return kafkaTemplate.send(
                auditProperties.kafka().transactionTopic(),
                event.transactionId().toString(),
                event
        ).thenApply(sendResult -> null);
    }
}
