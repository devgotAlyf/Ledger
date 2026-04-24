package com.example.ledger.repository;

import com.example.ledger.model.TransactionEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TransactionEventRepository extends JpaRepository<TransactionEventEntity, Long> {

    List<TransactionEventEntity> findByTransactionIdOrderByCreatedAtAsc(UUID transactionId);

    List<TransactionEventEntity> findAllByOrderByCreatedAtAsc();
}
