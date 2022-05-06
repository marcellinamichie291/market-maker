package com.bloxmove.marketmaker;

import com.bloxmove.marketmaker.model.ExchangeName;
import com.bloxmove.marketmaker.model.MarketMaker;
import com.bloxmove.marketmaker.model.MarketMakerRequest;
import com.bloxmove.marketmaker.model.MarketMakerShortRequest;
import com.bloxmove.marketmaker.model.OrderSide;
import com.bloxmove.marketmaker.model.OrderType;
import com.bloxmove.marketmaker.model.MarketMakerStatus;

import java.math.BigDecimal;
import java.time.Instant;

import static com.bloxmove.marketmaker.util.Constants.SCALE_ROUND;

public class TestUtils {

    public static final String ID = "0";
    public static final ExchangeName EXCHANGE_NAME = ExchangeName.BITMART;
    public static final String FIRST_CURRENCY = "BLXM";
    public static final String SECOND_CURRENCY = "USDT";
    public static final String CURRENCY_PAIR = FIRST_CURRENCY + "_" + SECOND_CURRENCY;
    public static final BigDecimal AMOUNT = BigDecimal.valueOf(80);
    public static final BigDecimal MIN_PRICE = BigDecimal.valueOf(0.9);
    public static final BigDecimal TARGET_PRICE = BigDecimal.valueOf(1);
    public static final BigDecimal MAX_PRICE = BigDecimal.valueOf(1.1);
    public static final Long DELAY = 5L;
    public static final Long GRID_COUNT = 3L;
    public static final BigDecimal MIN_FIRST_CURRENCY = BigDecimal.valueOf(5);
    public static final BigDecimal MIN_SECOND_CURRENCY = BigDecimal.valueOf(5);
    public static final MarketMakerStatus MARKET_MAKER_STATUS = MarketMakerStatus.WORKING;
    public static final Instant CREATED = Instant.ofEpochMilli(9);
    public static final Instant UPDATED = Instant.ofEpochMilli(10);

    public static final OrderSide SIDE = OrderSide.BUY;
    public static final OrderType TYPE = OrderType.LIMIT;
    public static final BigDecimal PRICE = new BigDecimal(11);


    public static final BigDecimal SINGLE_AMOUNT = AMOUNT.divide(new BigDecimal(GRID_COUNT, SCALE_ROUND), SCALE_ROUND)
            .divide(new BigDecimal(2, SCALE_ROUND), SCALE_ROUND);
    public static final BigDecimal MAX_PRICE_DELTA = MAX_PRICE.subtract(TARGET_PRICE, SCALE_ROUND);
    public static final BigDecimal MIN_PRICE_DELTA = TARGET_PRICE.subtract(MIN_PRICE, SCALE_ROUND);



    private TestUtils() {
    }

    public static MarketMakerRequest createMarketMakerRequest() {
        MarketMakerRequest marketMakerRequest = new MarketMakerRequest();
        marketMakerRequest.setExchangeName(EXCHANGE_NAME);
        marketMakerRequest.setCurrencyPair(CURRENCY_PAIR);
        marketMakerRequest.setAmount(AMOUNT);
        marketMakerRequest.setMinPrice(MIN_PRICE);
        marketMakerRequest.setTargetPrice(TARGET_PRICE);
        marketMakerRequest.setMaxPrice(MAX_PRICE);
        marketMakerRequest.setDelay(DELAY);
        marketMakerRequest.setGridCount(GRID_COUNT);
        marketMakerRequest.setMinFirstCurrency(MIN_FIRST_CURRENCY);
        marketMakerRequest.setMinSecondCurrency(MIN_SECOND_CURRENCY);
        return marketMakerRequest;
    }

    public static MarketMakerShortRequest createMarketMakerShortRequest() {
        MarketMakerShortRequest marketMakerShortRequest = new MarketMakerShortRequest();
        marketMakerShortRequest.setExchangeName(EXCHANGE_NAME);
        marketMakerShortRequest.setCurrencyPair(CURRENCY_PAIR);
        return marketMakerShortRequest;
    }

    public static MarketMaker createMarketMaker() {
        MarketMaker marketMaker = new MarketMaker();
        marketMaker.setId(ID);
        marketMaker.setExchangeName(EXCHANGE_NAME);
        marketMaker.setCurrencyPair(CURRENCY_PAIR);
        marketMaker.setAmount(AMOUNT);
        marketMaker.setMinPrice(MIN_PRICE);
        marketMaker.setTargetPrice(TARGET_PRICE);
        marketMaker.setMaxPrice(MAX_PRICE);
        marketMaker.setDelay(DELAY);
        marketMaker.setGridCount(GRID_COUNT);
        marketMaker.setMinFirstCurrency(MIN_FIRST_CURRENCY);
        marketMaker.setMinSecondCurrency(MIN_SECOND_CURRENCY);
        marketMaker.setStatus(MARKET_MAKER_STATUS);
        marketMaker.setCreated(CREATED);
        marketMaker.setUpdated(UPDATED);
        return marketMaker;
    }
}
