package com.bloxmove.marketmaker.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class MarketMakerShortRequest {

    @NotNull
    ExchangeName exchangeName;
    @NotNull
    String currencyPair;
    Boolean needCancelOrders;
}
