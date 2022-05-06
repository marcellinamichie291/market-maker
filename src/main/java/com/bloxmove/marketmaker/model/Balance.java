package com.bloxmove.marketmaker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Balance {

    private BigDecimal firstCurrencyBalance;
    private BigDecimal secondCurrencyBalance;

}
