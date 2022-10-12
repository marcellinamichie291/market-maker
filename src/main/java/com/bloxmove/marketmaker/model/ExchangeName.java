package com.bloxmove.marketmaker.model;

public enum ExchangeName {

    BITMART;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
