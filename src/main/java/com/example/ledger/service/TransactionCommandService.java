package com.example.ledger.service;

import com.example.ledger.kafka.TransactionEventPublisher;
import com.example.ledger.model.CreateTransactionRequest;
import com.example.ledger.model.EventType;
import com.example.ledger.model.TransactionAcceptedResponse;
import com.example.ledger.model.TransactionEvent;
import com.example.ledger.model.TransactionEventEntity;
import com.example.ledger.repository.TransactionEventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class TransactionCommandService {

    private final TransactionEventRepository transactionEventRepository;
    private final TransactionEventPublisher transactionEventPublisher;

    public TransactionCommandService(
            TransactionEventRepository transactionEventRepository,
            TransactionEventPublisher transactionEventPublisher
    ) {
        this.transactionEventRepository = transactionEventRepository;
        this.transactionEventPublisher = transactionEventPublisher;
    }

    public TransactionAcceptedResponse submit(CreateTransactionRequest request) {
        TransactionEvent event = new TransactionEvent(
                UUID.randomUUID(),
                request.userId(),
                request.amount(),
                request.merchant(),
                request.timestamp() == null ? Instant.now() : request.timestamp(),
                EventType.CREATED.name()
        );

        transactionEventRepository.save(
                TransactionEventEntity.append(event.transactionId(), EventType.CREATED, event, null)
        );
        transactionEventPublisher.publish(event).join();

        return new TransactionAcceptedResponse(event.transactionId(), EventType.CREATED.name());
    }
}
