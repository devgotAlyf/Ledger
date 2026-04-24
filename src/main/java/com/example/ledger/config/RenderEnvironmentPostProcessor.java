package com.example.ledger.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class RenderEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> derivedProperties = new LinkedHashMap<>();

        String datasourceUrl = environment.getProperty("spring.datasource.url");
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (StringUtils.hasText(databaseUrl) && !isExplicitDatasourceUrl(datasourceUrl)) {
            JdbcConnection jdbcConnection = toJdbcConnection(databaseUrl);
            derivedProperties.put("spring.datasource.url", jdbcConnection.url());

            if (shouldOverrideDatasourceUsername(environment) && StringUtils.hasText(jdbcConnection.username())) {
                derivedProperties.put("spring.datasource.username", jdbcConnection.username());
            }

            if (shouldOverrideDatasourcePassword(environment) && StringUtils.hasText(jdbcConnection.password())) {
                derivedProperties.put("spring.datasource.password", jdbcConnection.password());
            }
        }

        String redisUrl = environment.getProperty("REDIS_URL");
        if (StringUtils.hasText(redisUrl) && !StringUtils.hasText(environment.getProperty("spring.data.redis.url"))) {
            derivedProperties.put("spring.data.redis.url", redisUrl);
        }

        if (!derivedProperties.isEmpty()) {
            environment.getPropertySources()
                    .addFirst(new MapPropertySource("renderDerivedProperties", derivedProperties));
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean isExplicitDatasourceUrl(String datasourceUrl) {
        return StringUtils.hasText(datasourceUrl)
                && !datasourceUrl.equals("jdbc:postgresql://localhost:5432/ledger");
    }

    private boolean shouldOverrideDatasourceUsername(ConfigurableEnvironment environment) {
        String currentValue = environment.getProperty("spring.datasource.username");
        return !StringUtils.hasText(currentValue) || "postgres".equals(currentValue);
    }

    private boolean shouldOverrideDatasourcePassword(ConfigurableEnvironment environment) {
        String currentValue = environment.getProperty("spring.datasource.password");
        return !StringUtils.hasText(currentValue) || "postgres".equals(currentValue);
    }

    private JdbcConnection toJdbcConnection(String databaseUrl) {
        if (databaseUrl.startsWith("jdbc:")) {
            return new JdbcConnection(databaseUrl, null, null);
        }

        URI uri = URI.create(databaseUrl);
        String scheme = uri.getScheme();
        if (!"postgres".equalsIgnoreCase(scheme) && !"postgresql".equalsIgnoreCase(scheme)) {
            throw new IllegalStateException("Unsupported DATABASE_URL scheme: " + scheme);
        }

        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
                .append(uri.getHost());

        if (uri.getPort() > 0) {
            jdbcUrl.append(':').append(uri.getPort());
        }

        jdbcUrl.append(uri.getRawPath());

        if (StringUtils.hasText(uri.getRawQuery())) {
            jdbcUrl.append('?').append(uri.getRawQuery());
        }

        String username = null;
        String password = null;
        if (StringUtils.hasText(uri.getRawUserInfo())) {
            String[] credentials = uri.getRawUserInfo().split(":", 2);
            username = decode(credentials[0]);
            if (credentials.length > 1) {
                password = decode(credentials[1]);
            }
        }

        return new JdbcConnection(jdbcUrl.toString(), username, password);
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private record JdbcConnection(
            String url,
            String username,
            String password
    ) {
    }
}
