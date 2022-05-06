package com.bloxmove.marketmaker.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ExchangeName {

    @JsonProperty("bitmart")
    BITMART;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
