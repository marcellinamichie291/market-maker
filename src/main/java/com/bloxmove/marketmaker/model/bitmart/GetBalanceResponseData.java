package com.bloxmove.marketmaker.model.bitmart;

import lombok.Data;

import java.util.List;

@Data
public class GetBalanceResponseData {

    private List<SingleCurrencyResponseData> wallet;
}
