package com.bloxmove.marketmaker.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CurrencyBalance {

    private String name;
    private BigDecimal value;
}
