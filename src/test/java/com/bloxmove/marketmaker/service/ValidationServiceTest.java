package com.bloxmove.marketmaker.service;


import com.bloxmove.marketmaker.model.MarketMakerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

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
    void shouldThrowIllegalArgumentExceptionWhenMinPriceHigherThanOne() {
        marketMakerRequest.setMinPrice(BigDecimal.valueOf(1.05));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> validationService.validate(marketMakerRequest))
                .withMessage("Min price should be lower than 1");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTargetPriceIsNotInRange() {
        marketMakerRequest.setTargetPrice(BigDecimal.valueOf(1.2));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> validationService.validate(marketMakerRequest))
                .withMessage("Target price should be in range from 0.95 to 1.05");

        marketMakerRequest.setTargetPrice(BigDecimal.valueOf(0.8));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> validationService.validate(marketMakerRequest))
                .withMessage("Target price should be in range from 0.95 to 1.05");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenMaxPriceHigherLowerThanOne() {
        marketMakerRequest.setMaxPrice(BigDecimal.valueOf(0.8));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> validationService.validate(marketMakerRequest))
                .withMessage("Max price should be higher than 1");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenAmountLessThanZeroDotOne() {
        BigDecimal amount = BigDecimal.valueOf(0.1);
        marketMakerRequest.setAmount(amount);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> validationService.validate(marketMakerRequest))
                .withMessage("Amount should be more than 0.1");
    }
}