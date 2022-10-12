package com.bloxmove.marketmaker.service.client;

import com.bloxmove.marketmaker.model.Balance;
import com.bloxmove.marketmaker.model.Order;

import java.util.concurrent.CompletableFuture;

public interface ExchangeClient {

    CompletableFuture<Order> placeOrder(String currencyPair, String side, String type, String size, String price);

    CompletableFuture<Void> cancelOrders(String currencyPair, String side);

    CompletableFuture<Balance> getBalance();
}
