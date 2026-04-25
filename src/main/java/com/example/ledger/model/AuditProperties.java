package com.example.ledger.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "audit")
public record AuditProperties(
        Kafka kafka,
        Gemini gemini
) {
    public record Kafka(
            String transactionTopic
    ) {
    }

    public record Gemini(
            String apiKey,
            String baseUrl,
            String model,
            String version
    ) {
    }
}
