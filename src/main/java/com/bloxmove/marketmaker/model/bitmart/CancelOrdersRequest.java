package com.bloxmove.marketmaker.model.bitmart;

import lombok.Data;

@Data
public class CancelOrdersRequest {
    @ParamKey("symbol")
    private String symbol;
    @ParamKey("side")
    private String side;

    public CancelOrdersRequest symbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public CancelOrdersRequest side(String side) {
        this.side = side;
        return this;
    }
}
