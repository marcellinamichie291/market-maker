package com.bloxmove.marketmaker.model.bitmart;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PlaceOrderResponseData {

    @JsonProperty("order_id")
    private long orderId;
}
