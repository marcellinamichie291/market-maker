package com.bloxmove.marketmaker.controller.impl;

import com.bloxmove.marketmaker.controller.BalanceController;
import com.bloxmove.marketmaker.model.CurrencyBalance;
import com.bloxmove.marketmaker.model.ExchangeName;
import com.bloxmove.marketmaker.service.client.ExchangeClient;
import com.bloxmove.marketmaker.service.factory.ExchangeClientFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@RequestMapping(value = "/balance")
@RestController
@RequiredArgsConstructor
public class BalanceControllerImpl implements BalanceController {

    private final ExchangeClientFactory exchangeClientFactory;

    @Override
    public CompletableFuture<Collection<CurrencyBalance>> get(String exchangeName) {
        ExchangeClient exchangeClient = exchangeClientFactory.getExchangeClient(ExchangeName.valueOf(exchangeName));
        return exchangeClient.getBalance()
                .thenApply(balance -> balance.getCurrencies().values());
    }
}
