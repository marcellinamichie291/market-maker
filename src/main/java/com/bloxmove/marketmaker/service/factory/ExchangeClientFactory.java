package com.bloxmove.marketmaker.service.factory;

import com.bloxmove.marketmaker.model.ExchangeName;
import com.bloxmove.marketmaker.service.client.ExchangeClient;
import com.bloxmove.marketmaker.service.client.BitMartExchangeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangeClientFactory {

    private final BitMartExchangeClient bitMartExchangeClient;

    public ExchangeClient getExchangeClient(ExchangeName exchangeName) {
        switch (exchangeName) {
            case BITMART:
                return bitMartExchangeClient;
            default:
                throw new IllegalArgumentException(String.format("Exchange %s is not supported", exchangeName));
        }
    }
}
