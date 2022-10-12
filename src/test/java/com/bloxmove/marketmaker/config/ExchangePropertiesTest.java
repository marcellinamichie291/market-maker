package com.bloxmove.marketmaker.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.bloxmove.marketmaker.model.ExchangeName.BITMART;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ExchangePropertiesTest {

    @Autowired
    private ExchangeProperties exchangeProperties;

    @Test
    void shouldTestPropertiesValidation() {
        assertThat(exchangeProperties).isNotNull();
        assertThat(exchangeProperties.getExchange(BITMART.toString())).isNotNull();
        assertThat(exchangeProperties.getUrl(BITMART.toString())).isNotBlank();
        assertThat(exchangeProperties.getApiKey(BITMART.toString())).isNotBlank();
    }
}