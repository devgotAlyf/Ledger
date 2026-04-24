package com.example.ledger.service;

import com.example.ledger.model.AiAuditDecision;
import com.example.ledger.model.EventType;
import com.example.ledger.model.TransactionEvent;
import com.example.ledger.model.TransactionEventEntity;
import com.example.ledger.repository.TransactionEventRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class TransactionAuditService {

    private final TransactionEventRepository transactionEventRepository;
    private final AuditDecisionClient auditDecisionClient;

    public TransactionAuditService(
            TransactionEventRepository transactionEventRepository,
            AuditDecisionClient auditDecisionClient
    ) {
        this.transactionEventRepository = transactionEventRepository;
        this.auditDecisionClient = auditDecisionClient;
    }

    public void audit(TransactionEvent event) {
        if (!EventType.CREATED.name().equals(event.status()) || hasTerminalDecision(event.transactionId())) {
            return;
        }

        BigDecimal averageSpend = calculateAverageSpend(event);
        AiAuditDecision decision = auditDecisionClient.audit(event, averageSpend);
        TransactionEvent auditedEvent = new TransactionEvent(
                event.transactionId(),
                event.userId(),
                event.amount(),
                event.merchant(),
                event.timestamp(),
                decision.eventType().name()
        );

        transactionEventRepository.save(
                TransactionEventEntity.append(
                        auditedEvent.transactionId(),
                        decision.eventType(),
                        auditedEvent,
                        decision.aiDecision()
                )
        );
    }

    private boolean hasTerminalDecision(java.util.UUID transactionId) {
        return transactionEventRepository.findByTransactionIdOrderByCreatedAtAsc(transactionId).stream()
                .map(TransactionEventEntity::getEventType)
                .anyMatch(eventType -> eventType == EventType.APPROVED || eventType == EventType.FLAGGED || eventType == EventType.REJECTED);
    }

    private BigDecimal calculateAverageSpend(TransactionEvent event) {
        List<TransactionEventEntity> recentEvents = transactionEventRepository.findAllByOrderByCreatedAtAsc().stream()
                .filter(entity -> entity.getEventType() == EventType.CREATED)
                .filter(entity -> event.userId().equals(entity.getPayload().userId()))
                .filter(entity -> !event.transactionId().equals(entity.getTransactionId()))
                .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
                .limit(30)
                .toList();

        if (recentEvents.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal total = recentEvents.stream()
                .map(TransactionEventEntity::getPayload)
                .map(TransactionEvent::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(BigDecimal.valueOf(recentEvents.size()), 2, RoundingMode.HALF_UP);
    }
}
