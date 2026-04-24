package com.example.ledger.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "audit")
public record AuditProperties(
        Kafka kafka,
        Claude claude
) {
    public record Kafka(
            String transactionTopic
    ) {
    }

    public record Claude(
            String apiKey,
            String baseUrl,
            String model,
            String version
    ) {
    }
}
