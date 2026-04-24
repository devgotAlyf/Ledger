package com.example.ledger.controller;

import com.example.ledger.model.CreateTransactionRequest;
import com.example.ledger.model.TransactionAcceptedResponse;
import com.example.ledger.model.TransactionHistoryItem;
import com.example.ledger.model.TransactionReplayResponse;
import com.example.ledger.service.TransactionCommandService;
import com.example.ledger.service.TransactionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transactions", description = "Transaction ingestion, history, and replay APIs")
public class TransactionController {

    private final TransactionCommandService transactionCommandService;
    private final TransactionQueryService transactionQueryService;

    public TransactionController(
            TransactionCommandService transactionCommandService,
            TransactionQueryService transactionQueryService
    ) {
        this.transactionCommandService = transactionCommandService;
        this.transactionQueryService = transactionQueryService;
    }

    @PostMapping
    @Operation(
            summary = "Create a transaction",
            description = "Publishes a transaction event and appends it to the event store.",
            responses = {
                    @ApiResponse(
                            responseCode = "202",
                            description = "Transaction accepted",
                            content = @Content(
                                    schema = @Schema(implementation = TransactionAcceptedResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "transactionId": "123e4567-e89b-12d3-a456-426614174000",
                                                      "status": "CREATED"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request")
            }
    )
    public ResponseEntity<TransactionAcceptedResponse> create(@Valid @RequestBody CreateTransactionRequest request) {
        return ResponseEntity.accepted().body(transactionCommandService.submit(request));
    }

    @GetMapping("/{userId}/history")
    @Operation(
            summary = "Get user history",
            description = "Returns all stored events for a user in ascending event time order."
    )
    public List<TransactionHistoryItem> history(@PathVariable String userId) {
        return transactionQueryService.history(userId);
    }

    @GetMapping("/{transactionId}/replay")
    @Operation(
            summary = "Replay a transaction",
            description = "Reconstructs transaction state at a given timestamp from the append-only event log.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Replay completed"),
                    @ApiResponse(responseCode = "404", description = "Transaction not found")
            }
    )
    public TransactionReplayResponse replay(
            @PathVariable UUID transactionId,
            @Parameter(example = "2026-04-23T15:30:00Z") @RequestParam Instant at
    ) {
        return transactionQueryService.replay(transactionId, at);
    }
}
