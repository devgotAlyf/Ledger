package com.example.ledger.service;

import com.example.ledger.model.AiAuditDecision;
import com.example.ledger.model.TransactionEvent;

import java.math.BigDecimal;

public interface AuditDecisionClient {

    AiAuditDecision audit(TransactionEvent event, BigDecimal averageSpend);
}
