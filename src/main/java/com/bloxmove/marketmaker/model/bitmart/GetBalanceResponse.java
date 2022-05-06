package com.bloxmove.marketmaker.model.bitmart;

import lombok.Data;

@Data
public class GetBalanceResponse {

    private String message;
    private int code;
    private String trace;
    private GetBalanceResponseData data;
}
