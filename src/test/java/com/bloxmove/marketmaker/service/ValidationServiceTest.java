package com.bloxmove.marketmaker.service;


import com.bloxmove.marketmaker.model.MarketMakerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.bloxmove.marketmaker.TestUtils.MAX_PRICE;
import static com.bloxmove.marketmaker.TestUtils.TARGET_PRICE;
import static com.bloxmove.marketmaker.TestUtils.createMarketMakerRequest;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ValidationServiceTest {

    private ValidationService validationService;
    private MarketMakerRequest marketMakerRequest;

    @BeforeEach
    void setUp() {
        validationService = new ValidationServiceImpl();
        marketMakerRequest = createMarketMakerRequest();
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenMinPriceHigherThanTargetPrice() {
        BigDecimal minPrice = BigDecimal.valueOf(999);
        marketMakerRequest.setMinPrice(minPrice);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> validationService.validate(marketMakerRequest))
                .withMessage(String.format("Min price '%s' should be lower than target price '%s'",
                        minPrice, TARGET_PRICE));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTargetPriceHigherThanMaxPrice() {
        BigDecimal targetPrice = BigDecimal.valueOf(999);
        marketMakerRequest.setTargetPrice(targetPrice);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> validationService.validate(marketMakerRequest))
                .withMessage(String.format("Target price '%s' should be lower than max price '%s'",
                        targetPrice, MAX_PRICE));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenAmountLessThanZeroDotOne() {
        BigDecimal amount = BigDecimal.valueOf(0.1);
        marketMakerRequest.setAmount(amount);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> validationService.validate(marketMakerRequest))
                .withMessage("Amount should be more than 0.1");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenAmountMultipliedByPriceLessThanFive() {
        BigDecimal amount = BigDecimal.valueOf(12);
        marketMakerRequest.setAmount(amount);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> validationService.validate(marketMakerRequest))
                .withMessage("Single order amount*price should be more than 5");
    }
}