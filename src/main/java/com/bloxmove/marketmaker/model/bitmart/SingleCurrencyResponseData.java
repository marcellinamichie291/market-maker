package com.bloxmove.marketmaker.model.bitmart;

import lombok.Data;

@Data
public class SingleCurrencyResponseData {

    private String currency;
    private String name;
    private String available;
    private String frozen;
}
