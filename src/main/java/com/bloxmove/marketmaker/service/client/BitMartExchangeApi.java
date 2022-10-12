package com.bloxmove.marketmaker.service.client;

import com.bloxmove.marketmaker.model.bitmart.CancelOrdersRequest;
import com.bloxmove.marketmaker.model.bitmart.GetBalanceResponse;
import com.bloxmove.marketmaker.model.bitmart.PlaceOrderResponse;
import com.bloxmove.marketmaker.model.bitmart.SubmitOrderRequest;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

import java.util.concurrent.CompletableFuture;

public interface BitMartExchangeApi {

    @POST("/spot/v1/submit_order")
    CompletableFuture<PlaceOrderResponse> placeOrder(@Header("X-BM-KEY") String apiKey,
                                                     @Header("X-BM-TIMESTAMP") String timestamp,
                                                     @Header("X-BM-SIGN") String sign,
                                                     @Body SubmitOrderRequest submitOrderRequest);

    @POST("/spot/v1/cancel_orders")
    CompletableFuture<Void> cancelOrders(@Header("X-BM-KEY") String apiKey,
                                         @Header("X-BM-TIMESTAMP") String timestamp,
                                         @Header("X-BM-SIGN") String sign,
                                         @Body CancelOrdersRequest cancelOrdersRequest);

    @GET("/account/v1/wallet")
    CompletableFuture<GetBalanceResponse> getBalance(@Header("X-BM-KEY") String apiKey);

}