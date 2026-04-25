package com.example.ledger.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

public class KafkaConfiguredCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String bootstrapServers = context.getEnvironment().getProperty("KAFKA_BOOTSTRAP_SERVERS");

        if (!StringUtils.hasText(bootstrapServers)) {
            bootstrapServers = context.getEnvironment().getProperty("spring.kafka.bootstrap-servers");
        }

        return StringUtils.hasText(bootstrapServers);
    }
}
