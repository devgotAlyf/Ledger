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
public class GeminiAuditClient implements AuditDecisionClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private final AuditProperties.Gemini properties;
    private final WebClient webClient;

    public GeminiAuditClient(WebClient.Builder webClientBuilder, AuditProperties auditProperties) {
        this.properties = auditProperties.gemini();
        this.webClient = webClientBuilder
                .baseUrl(this.properties.baseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public AiAuditDecision audit(TransactionEvent event, BigDecimal averageSpend) {
        if (!StringUtils.hasText(properties.apiKey())) {
            throw new IllegalStateException("GEMINI_API_KEY is required");
        }

        GeminiResponse response = webClient.post()
                .uri("/{version}/models/{model}:generateContent", properties.version(), properties.model())
                .header("x-goog-api-key", properties.apiKey())
                .bodyValue(new GeminiRequest(
                        new SystemInstruction(List.of(new Part("You are a financial auditor."))),
                        List.of(new Content(List.of(new Part(buildPrompt(event, averageSpend))))),
                        new GenerationConfig(0, "text/plain")
                ))
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .block(TIMEOUT);

        String reply = Optional.ofNullable(response)
                .map(GeminiResponse::candidates)
                .filter(candidates -> !candidates.isEmpty())
                .map(candidates -> candidates.get(0))
                .map(Candidate::content)
                .map(Content::parts)
                .filter(parts -> !parts.isEmpty())
                .map(parts -> parts.get(0).text())
                .orElse("");

        return parseDecision(reply);
    }

    private String buildPrompt(TransactionEvent event, BigDecimal averageSpend) {
        return """
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
                EventType.FLAGGED.name() + ": Gemini response could not be parsed",
                "Gemini response could not be parsed"
        );
    }

    private record GeminiRequest(
            @JsonProperty("system_instruction") SystemInstruction systemInstruction,
            List<Content> contents,
            GenerationConfig generationConfig
    ) {
    }

    private record SystemInstruction(
            List<Part> parts
    ) {
    }

    private record Content(
            List<Part> parts
    ) {
    }

    private record Part(
            String text
    ) {
    }

    private record GenerationConfig(
            double temperature,
            String responseMimeType
    ) {
    }

    private record GeminiResponse(
            List<Candidate> candidates
    ) {
    }

    private record Candidate(
            Content content
    ) {
    }
}
