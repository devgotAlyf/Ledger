package com.example.ledger.kafka;

import com.example.ledger.model.TransactionEvent;

import java.util.concurrent.CompletableFuture;

public interface TransactionEventPublisher {

    CompletableFuture<Void> publish(TransactionEvent event);
}
