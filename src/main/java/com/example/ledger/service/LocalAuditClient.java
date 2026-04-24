package com.example.ledger.service;

import com.example.ledger.model.AiAuditDecision;
import com.example.ledger.model.EventType;
import com.example.ledger.model.TransactionEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Profile("local")
public class LocalAuditClient implements AuditDecisionClient {

    @Override
    public AiAuditDecision audit(TransactionEvent event, BigDecimal averageSpend) {
        if (averageSpend.signum() > 0 && event.amount().compareTo(averageSpend.multiply(BigDecimal.valueOf(2))) > 0) {
            return new AiAuditDecision(
                    EventType.FLAGGED,
                    EventType.FLAGGED.name() + ": amount exceeds 2x average spend",
                    "amount exceeds 2x average spend"
            );
        }

        return new AiAuditDecision(EventType.APPROVED, EventType.APPROVED.name(), null);
    }
}
