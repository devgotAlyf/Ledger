package com.example.ledger.service;

import com.example.ledger.model.TransactionEvent;
import com.example.ledger.model.TransactionEventEntity;
import com.example.ledger.model.TransactionHistoryItem;
import com.example.ledger.model.TransactionReplayResponse;
import com.example.ledger.repository.TransactionEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionQueryService {

    private final TransactionEventRepository transactionEventRepository;

    public TransactionQueryService(TransactionEventRepository transactionEventRepository) {
        this.transactionEventRepository = transactionEventRepository;
    }

    public List<TransactionHistoryItem> history(String userId) {
        return transactionEventRepository.findAllByOrderByCreatedAtAsc().stream()
                .filter(entity -> userId.equals(entity.getPayload().userId()))
                .map(TransactionHistoryItem::from)
                .toList();
    }

    public TransactionReplayResponse replay(UUID transactionId, Instant at) {
        List<TransactionEventEntity> events = transactionEventRepository.findByTransactionIdOrderByCreatedAtAsc(transactionId).stream()
                .filter(event -> !event.getCreatedAt().isAfter(at))
                .toList();

        if (events.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found");
        }

        TransactionEventEntity latest = events.get(events.size() - 1);
        TransactionEvent payload = latest.getPayload();

        return new TransactionReplayResponse(
                transactionId,
                payload.userId(),
                payload.amount(),
                payload.merchant(),
                payload.timestamp(),
                latest.getEventType(),
                latest.getAiDecision(),
                events.stream().map(TransactionHistoryItem::from).toList()
        );
    }
}
