package com.bloxmove.marketmaker.model.bitmart;

import lombok.Data;

@Data
public class SubmitOrderRequest {
    @ParamKey("symbol")
    private String symbol;
    @ParamKey("side")
    private String side;
    @ParamKey("type")
    private String type;
    @ParamKey("size")
    private String size;
    @ParamKey("price")
    private String price;
    @ParamKey("notional")
    private String notional;

    public SubmitOrderRequest symbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public SubmitOrderRequest side(String side) {
        this.side = side;
        return this;
    }

    public SubmitOrderRequest type(String type) {
        this.type = type;
        return this;
    }

    public SubmitOrderRequest size(String size) {
        this.size = size;
        return this;
    }

    public SubmitOrderRequest price(String price) {
        this.price = price;
        return this;
    }

    public SubmitOrderRequest notional(String notional) {
        this.notional = notional;
        return this;
    }
}
