package com.bloxmove.marketmaker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Validated
@ConfigurationProperties(prefix = "application")
@ConstructorBinding
public class ExchangeProperties {

    @NotNull
    private final Map<String, List<Exchange>> exchange;

    public ExchangeProperties(Map<String, List<Exchange>> exchange) {
        this.exchange = exchange;
    }

    public Exchange getExchange(String exchangeName) {
        List<Exchange> exchanges = exchange.get(exchangeName);
        return exchanges.get(new Random().nextInt(exchanges.size()));
    }

    public String getUrl(String exchangeName) {
        return getExchange(exchangeName).getUrl();
    }

    public String getApiKey(String exchangeName) {
        return getExchange(exchangeName).getApiKey();
    }

    @Data
    public static class Exchange {

        @NotBlank
        private final String url;
        @NotBlank
        private final String apiKey;
        @NotBlank
        private final String apiSecret;
        @NotBlank
        private final String memo;
    }
}
