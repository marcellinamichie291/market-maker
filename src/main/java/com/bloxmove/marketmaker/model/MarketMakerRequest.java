package com.bloxmove.marketmaker.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
public class MarketMakerRequest {

    @NotNull
    ExchangeName exchangeName;
    @NotNull
    String currencyPair;
    @NotNull
    BigDecimal amount;
    @NotNull
    BigDecimal minPrice;
    @NotNull
    BigDecimal targetPrice;
    @NotNull
    BigDecimal maxPrice;
    @NotNull
    @Min(1)
    Long delay;
    @NotNull
    @Min(1)
    Long gridCount;
    @NotNull
    BigDecimal minFirstCurrency;
    @NotNull
    BigDecimal minSecondCurrency;
}
