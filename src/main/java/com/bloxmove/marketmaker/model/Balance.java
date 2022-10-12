package com.bloxmove.marketmaker.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Balance {

    private Map<String, CurrencyBalance> currencies = new HashMap<>();

    public Balance addCurrency(String name, CurrencyBalance currencyBalance) {
        this.currencies.put(name, currencyBalance);
        return this;
    }

    public CurrencyBalance getCurrencyBalance(String name) {
        return this.currencies.get(name);
    }

}
