package com.bloxmove.marketmaker.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

@Document
@Data
public class MarketMaker {

    @Id
    @NotNull
    private String id;
    @NotNull
    private ExchangeName exchangeName;
    @NotNull
    private String currencyPair;
    @NotNull
    private BigDecimal amount;
    @NotNull
    private BigDecimal minPrice;
    @NotNull
    private BigDecimal targetPrice;
    @NotNull
    private BigDecimal maxPrice;
    @NotNull
    @Min(1)
    private Long delay;
    @NotNull
    @Min(1)
    private Long gridCount;
    @NotNull
    private BigDecimal minFirstCurrency;
    @NotNull
    private BigDecimal minSecondCurrency;
    @NotNull
    private MarketMakerStatus status;
    @NotNull
    private Instant created;
    private Instant updated;

    public MarketMaker id(String id) {
        this.id = id;
        return this;
    }

    public MarketMaker status(MarketMakerStatus status) {
        this.status = status;
        return this;
    }

    public MarketMaker created(Instant created) {
        this.created = created;
        return this;
    }

    public MarketMaker updated(Instant updated) {
        this.updated = updated;
        return this;
    }

}
