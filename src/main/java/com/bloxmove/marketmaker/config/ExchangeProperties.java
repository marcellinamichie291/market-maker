package com.bloxmove.marketmaker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "application")
@ConstructorBinding
public class ExchangeProperties {

    @NotNull
    private final Map<String, Exchange> exchange;

    public ExchangeProperties(Map<String, Exchange> exchange) {
        this.exchange = exchange;
    }

    public String getApiKey(String exchangeName) {
        return exchange.get(exchangeName).getApiKey();
    }

    public String getApiSecret(String exchangeName) {
        return exchange.get(exchangeName).getApiSecret();
    }

    public String getMemo(String exchangeName) {
        return exchange.get(exchangeName).getMemo();
    }

    @Data
    private static class Exchange {

        @NotBlank
        private final String apiKey;
        @NotBlank
        private final String apiSecret;
        @NotBlank
        private final String memo;
    }
}
