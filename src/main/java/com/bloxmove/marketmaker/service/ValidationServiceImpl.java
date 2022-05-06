package com.bloxmove.marketmaker.service;

import com.bloxmove.marketmaker.model.MarketMakerRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.bloxmove.marketmaker.util.Constants.SCALE_ROUND;

@Service
public class ValidationServiceImpl implements ValidationService {

    private static final String MIN_HIGHER_TARGET_EXC = "Min price '%s' should be lower than target price '%s'";
    private static final String TARGET_HIGHER_MAX_EXC = "Target price '%s' should be lower than max price '%s'";
    private static final String MIN_SIZE_EXC = "Amount should be more than 0.1";
    private static final String MIN_SIZE_PRICE_EXC = "Single order amount*price should be more than 5";

    @Override
    public void validate(MarketMakerRequest marketMakerRequest) {
        BigDecimal gridCount = BigDecimal.valueOf(marketMakerRequest.getGridCount());
        BigDecimal singleAmount = marketMakerRequest.getAmount().divide(gridCount, SCALE_ROUND)
                .divide(BigDecimal.valueOf(2), SCALE_ROUND);

        BigDecimal minPrice = marketMakerRequest.getMinPrice();
        BigDecimal targetPrice = marketMakerRequest.getTargetPrice();
        BigDecimal maxPrice = marketMakerRequest.getMaxPrice();
        BigDecimal sizeForMinPrice = singleAmount.multiply(minPrice, SCALE_ROUND);

        if (minPrice.compareTo(targetPrice) > 0) {
            throw new IllegalArgumentException(String.format(MIN_HIGHER_TARGET_EXC, minPrice, targetPrice));
        }
        if (targetPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException(String.format(TARGET_HIGHER_MAX_EXC, targetPrice, maxPrice));
        }
        if (singleAmount.compareTo(BigDecimal.valueOf(0.1)) < 0) {
            throw new IllegalArgumentException(MIN_SIZE_EXC);
        }
        if (sizeForMinPrice.compareTo(BigDecimal.valueOf(5)) < 0) {
            throw new IllegalArgumentException(MIN_SIZE_PRICE_EXC);
        }
    }
}
