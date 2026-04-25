package com.example.ledger.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class KafkaNotConfiguredCondition implements Condition {

    private final KafkaConfiguredCondition kafkaConfiguredCondition = new KafkaConfiguredCondition();

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return !kafkaConfiguredCondition.matches(context, metadata);
    }
}
