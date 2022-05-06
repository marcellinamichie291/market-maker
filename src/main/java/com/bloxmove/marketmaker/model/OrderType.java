package com.bloxmove.marketmaker.model;

public enum OrderType {
    LIMIT,
    MARKET,
    LIMIT_MAKER,
    IOC;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}