package com.bloxmove.marketmaker.service.mapper;

import com.bloxmove.marketmaker.model.MarketMaker;
import com.bloxmove.marketmaker.model.MarketMakerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.bloxmove.marketmaker.TestUtils.AMOUNT;
import static com.bloxmove.marketmaker.TestUtils.CURRENCY_PAIR;
import static com.bloxmove.marketmaker.TestUtils.DELAY;
import static com.bloxmove.marketmaker.TestUtils.EXCHANGE_NAME;
import static com.bloxmove.marketmaker.TestUtils.GRID_COUNT;
import static com.bloxmove.marketmaker.TestUtils.MAX_PRICE;
import static com.bloxmove.marketmaker.TestUtils.MIN_FIRST_CURRENCY;
import static com.bloxmove.marketmaker.TestUtils.MIN_PRICE;
import static com.bloxmove.marketmaker.TestUtils.MIN_SECOND_CURRENCY;
import static com.bloxmove.marketmaker.TestUtils.TARGET_PRICE;
import static com.bloxmove.marketmaker.TestUtils.createMarketMaker;
import static com.bloxmove.marketmaker.TestUtils.createMarketMakerRequest;
import static org.assertj.core.api.Assertions.assertThat;

class MarketMakerMapperTest {

    private MarketMakerRequest marketMakerRequest;
    private MarketMaker marketMaker;

    @BeforeEach
    void setUp() {
        marketMakerRequest = createMarketMakerRequest();
        marketMaker = createMarketMaker();
    }

    @Test
    void shouldMapMarketMakerRequestToMarketMaker() {
        MarketMaker marketMaker = MarketMakerMapper.INSTANCE.map(marketMakerRequest);
        assertThat(marketMaker)
                .hasFieldOrPropertyWithValue("exchangeName", EXCHANGE_NAME)
                .hasFieldOrPropertyWithValue("currencyPair", CURRENCY_PAIR)
                .hasFieldOrPropertyWithValue("amount", AMOUNT)
                .hasFieldOrPropertyWithValue("minPrice", MIN_PRICE)
                .hasFieldOrPropertyWithValue("targetPrice", TARGET_PRICE)
                .hasFieldOrPropertyWithValue("maxPrice", MAX_PRICE)
                .hasFieldOrPropertyWithValue("delay", DELAY)
                .hasFieldOrPropertyWithValue("gridCount", GRID_COUNT)
                .hasFieldOrPropertyWithValue("minFirstCurrency", MIN_FIRST_CURRENCY)
                .hasFieldOrPropertyWithValue("minSecondCurrency", MIN_SECOND_CURRENCY)
                .hasFieldOrPropertyWithValue("id", null)
                .hasFieldOrPropertyWithValue("status", null)
                .hasFieldOrPropertyWithValue("created", null)
                .hasFieldOrPropertyWithValue("updated", null);
    }

    @Test
    void shouldMapMarketMakerRequestToMarketMakerWhenMarketMakerRequestFieldsAreNull() {
        MarketMaker marketMaker = MarketMakerMapper.INSTANCE.map(new MarketMakerRequest());
        assertThat(marketMaker)
                .hasFieldOrPropertyWithValue("exchangeName", null)
                .hasFieldOrPropertyWithValue("currencyPair", null)
                .hasFieldOrPropertyWithValue("amount", null)
                .hasFieldOrPropertyWithValue("minPrice", null)
                .hasFieldOrPropertyWithValue("targetPrice", null)
                .hasFieldOrPropertyWithValue("maxPrice", null)
                .hasFieldOrPropertyWithValue("delay", null)
                .hasFieldOrPropertyWithValue("gridCount", null)
                .hasFieldOrPropertyWithValue("minFirstCurrency", null)
                .hasFieldOrPropertyWithValue("minSecondCurrency", null)
                .hasFieldOrPropertyWithValue("id", null)
                .hasFieldOrPropertyWithValue("status", null)
                .hasFieldOrPropertyWithValue("created", null)
                .hasFieldOrPropertyWithValue("updated", null);
    }

    @Test
    void shouldMapMarketMakerToMarketMakerRequest() {
        MarketMakerRequest marketMakerRequest = MarketMakerMapper.INSTANCE.map(marketMaker);
        assertThat(marketMakerRequest)
                .hasFieldOrPropertyWithValue("exchangeName", EXCHANGE_NAME)
                .hasFieldOrPropertyWithValue("currencyPair", CURRENCY_PAIR)
                .hasFieldOrPropertyWithValue("amount", AMOUNT)
                .hasFieldOrPropertyWithValue("minPrice", MIN_PRICE)
                .hasFieldOrPropertyWithValue("targetPrice", TARGET_PRICE)
                .hasFieldOrPropertyWithValue("maxPrice", MAX_PRICE)
                .hasFieldOrPropertyWithValue("delay", DELAY)
                .hasFieldOrPropertyWithValue("gridCount", GRID_COUNT)
                .hasFieldOrPropertyWithValue("minFirstCurrency", MIN_FIRST_CURRENCY)
                .hasFieldOrPropertyWithValue("minSecondCurrency", MIN_SECOND_CURRENCY);
    }

    @Test
    void shouldMapMarketMakerToMarketMakerRequestWhenMarketMakerFieldsAreNull() {
        MarketMakerRequest marketMakerRequest = MarketMakerMapper.INSTANCE.map(new MarketMaker());
        assertThat(marketMakerRequest)
                .hasFieldOrPropertyWithValue("exchangeName", null)
                .hasFieldOrPropertyWithValue("currencyPair", null)
                .hasFieldOrPropertyWithValue("amount", null)
                .hasFieldOrPropertyWithValue("minPrice", null)
                .hasFieldOrPropertyWithValue("targetPrice", null)
                .hasFieldOrPropertyWithValue("maxPrice", null)
                .hasFieldOrPropertyWithValue("delay", null)
                .hasFieldOrPropertyWithValue("gridCount", null)
                .hasFieldOrPropertyWithValue("minFirstCurrency", null)
                .hasFieldOrPropertyWithValue("minSecondCurrency", null);
    }

}