package com.example.ledger.service;

import com.example.ledger.model.AiAuditDecision;
import com.example.ledger.model.AuditProperties;
import com.example.ledger.model.EventType;
import com.example.ledger.model.TransactionEvent;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Profile("!local")
public class ClaudeAuditClient implements AuditDecisionClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private final AuditProperties.Claude properties;
    private final WebClient webClient;

    public ClaudeAuditClient(WebClient.Builder webClientBuilder, AuditProperties auditProperties) {
        this.properties = auditProperties.claude();
        this.webClient = webClientBuilder
                .baseUrl(this.properties.baseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public AiAuditDecision audit(TransactionEvent event, BigDecimal averageSpend) {
        if (!StringUtils.hasText(properties.apiKey())) {
            throw new IllegalStateException("CLAUDE_API_KEY is required");
        }

        ClaudeResponse response = webClient.post()
                .uri("/v1/messages")
                .header("x-api-key", properties.apiKey())
                .header("anthropic-version", properties.version())
                .bodyValue(new ClaudeRequest(
                        properties.model(),
                        128,
                        0,
                        List.of(new ClaudeMessage("user", buildPrompt(event, averageSpend)))
                ))
                .retrieve()
                .bodyToMono(ClaudeResponse.class)
                .block(TIMEOUT);

        String reply = Optional.ofNullable(response)
                .map(ClaudeResponse::content)
                .filter(content -> !content.isEmpty())
                .map(content -> content.get(0).text())
                .orElse("");

        return parseDecision(reply);
    }

    private String buildPrompt(TransactionEvent event, BigDecimal averageSpend) {
        return """
                You are a financial auditor.
                Amount: \u20b9%s
                Merchant: %s
                Avg spend: \u20b9%s
                Time: %s

                Reply:
                APPROVED
                or
                FLAGGED + reason
                """.formatted(
                event.amount().toPlainString(),
                event.merchant(),
                averageSpend.toPlainString(),
                event.timestamp()
        );
    }

    private AiAuditDecision parseDecision(String rawReply) {
        String normalized = rawReply == null ? "" : rawReply.trim();
        String upper = normalized.toUpperCase(Locale.ROOT);

        if (upper.startsWith(EventType.APPROVED.name())) {
            return new AiAuditDecision(EventType.APPROVED, EventType.APPROVED.name(), null);
        }

        if (upper.startsWith(EventType.FLAGGED.name())) {
            String reason = normalized.substring(EventType.FLAGGED.name().length())
                    .replaceFirst("^[\\s:+-]+", "")
                    .trim();
            String decision = reason.isEmpty() ? EventType.FLAGGED.name() : EventType.FLAGGED.name() + ": " + reason;
            return new AiAuditDecision(EventType.FLAGGED, decision, reason.isEmpty() ? null : reason);
        }

        return new AiAuditDecision(
                EventType.FLAGGED,
                EventType.FLAGGED.name() + ": Claude response could not be parsed",
                "Claude response could not be parsed"
        );
    }

    private record ClaudeRequest(
            String model,
            @JsonProperty("max_tokens") int maxTokens,
            double temperature,
            List<ClaudeMessage> messages
    ) {
    }

    private record ClaudeMessage(
            String role,
            String content
    ) {
    }

    private record ClaudeResponse(
            List<ClaudeContent> content
    ) {
    }

    private record ClaudeContent(
            String type,
            String text
    ) {
    }
}
