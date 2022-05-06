package com.bloxmove.marketmaker.service.client;

import com.bloxmove.marketmaker.model.Balance;
import com.bloxmove.marketmaker.model.Order;

public interface ExchangeClient {

    Order placeOrder(String currencyPair, String side, String type, String size, String price);

    Balance getBalance(String firstCurrency, String secondCurrency);
}
