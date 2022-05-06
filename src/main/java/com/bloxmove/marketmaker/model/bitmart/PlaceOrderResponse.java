package com.bloxmove.marketmaker.model.bitmart;

import lombok.Data;

@Data
public class PlaceOrderResponse {

    private String message;
    private int code;
    private String trace;
    private PlaceOrderResponseData data;
}
