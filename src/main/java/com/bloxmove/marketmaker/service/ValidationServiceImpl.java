package com.bloxmove.marketmaker.service;

import com.bloxmove.marketmaker.model.MarketMakerRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.bloxmove.marketmaker.util.Constants.SCALE_ROUND;

@Service
public class ValidationServiceImpl implements ValidationService {

    private static final String MIN_PRICE_EXC = "Min price should be lower than 1";
    private static final String TARGET_PRICE_EXC = "Target price should be in range from 0.95 to 1.05";
    private static final String MAX_PRICE_EXC = "Max price should be higher than 1";
    private static final String MIN_SIZE_EXC = "Amount should be more than 0.1";

    @Override
    public void validate(MarketMakerRequest marketMakerRequest) {
        BigDecimal gridCount = BigDecimal.valueOf(marketMakerRequest.getGridCount());
        BigDecimal singleAmount = marketMakerRequest.getAmount().divide(gridCount, SCALE_ROUND)
                .divide(BigDecimal.valueOf(2), SCALE_ROUND);
        BigDecimal targetPrice = marketMakerRequest.getTargetPrice();

        if (marketMakerRequest.getMinPrice().compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(MIN_PRICE_EXC);
        }
        if (targetPrice.compareTo(BigDecimal.valueOf(1.05)) > 0
                || targetPrice.compareTo(BigDecimal.valueOf(0.95)) < 0) {
            throw new IllegalArgumentException(TARGET_PRICE_EXC);
        }
        if (marketMakerRequest.getMaxPrice().compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException(MAX_PRICE_EXC);
        }
        if (singleAmount.compareTo(BigDecimal.valueOf(0.1)) < 0) {
            throw new IllegalArgumentException(MIN_SIZE_EXC);
        }
    }
}
