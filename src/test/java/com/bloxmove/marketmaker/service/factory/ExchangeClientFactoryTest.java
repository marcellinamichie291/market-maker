package com.bloxmove.marketmaker.service.factory;

import com.bloxmove.marketmaker.model.ExchangeName;
import com.bloxmove.marketmaker.service.client.BitMartExchangeClient;
import com.bloxmove.marketmaker.service.client.ExchangeClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ExchangeClientFactoryTest {

    @Autowired
    private ExchangeClientFactory exchangeClientFactory;

    @ParameterizedTest
    @MethodSource("exchangeNameArguments")
    void shouldReturnExchangeClientBasedOnExchangeName(ExchangeName exchangeName, Class<ExchangeClient> instance) {
        assertThat(exchangeClientFactory.getExchangeClient(exchangeName))
                .isInstanceOf(instance);
    }
    private static Stream<Arguments> exchangeNameArguments() {
        return Stream.of(
                Arguments.of(ExchangeName.BITMART, BitMartExchangeClient.class)
        );
    }

}