package com.example.ledger.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, Object> index() {
        return Map.of(
                "service", "ledger",
                "status", "UP",
                "endpoints", Map.of(
                        "createTransaction", "POST /transactions",
                        "history", "GET /transactions/{userId}/history",
                        "replay", "GET /transactions/{transactionId}/replay?at=2026-04-23T14:53:24Z",
                        "swaggerUi", "GET /swagger-ui.html",
                        "openApi", "GET /v3/api-docs"
                )
        );
    }

    @GetMapping("/healthz")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
