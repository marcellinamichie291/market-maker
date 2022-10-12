package com.bloxmove.marketmaker.service.client;

import com.bloxmove.marketmaker.config.ExchangeProperties;
import com.bloxmove.marketmaker.model.Balance;
import com.bloxmove.marketmaker.model.CurrencyBalance;
import com.bloxmove.marketmaker.model.Order;
import com.bloxmove.marketmaker.model.bitmart.CancelOrdersRequest;
import com.bloxmove.marketmaker.model.bitmart.SubmitOrderRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.bloxmove.marketmaker.model.ExchangeName.BITMART;

@Component
@Log4j2
public class BitMartExchangeClient implements ExchangeClient {

    private final ObjectMapper objectMapper;
    private final ExchangeProperties exchangeProperties;
    private final BitMartExchangeApi bitMartExchangeApi;

    public BitMartExchangeClient(ObjectMapper objectMapper, ExchangeProperties exchangeProperties) {
        this.objectMapper = objectMapper;
        this.exchangeProperties = exchangeProperties;
        Retrofit retrofit = createRetrofit();
        this.bitMartExchangeApi = retrofit.create(BitMartExchangeApi.class);
    }

    @Override
    public CompletableFuture<Order> placeOrder(String currencyPair, String side, String type, String size, String price) {
        ExchangeProperties.Exchange exchange = exchangeProperties.getExchange(BITMART.toString());
        String timestamp = String.valueOf(System.currentTimeMillis());
        SubmitOrderRequest request = new SubmitOrderRequest()
                .symbol(currencyPair)
                .side(side)
                .type(type)
                .size(size)
                .price(price);
        String sign = createSha256Signature(timestamp, request, exchange);
        return bitMartExchangeApi.placeOrder(exchange.getApiKey(), timestamp, sign, request)
                .thenApply(placeOrderResponse -> new Order(placeOrderResponse.getData().getOrderId()));
    }

    @Override
    public CompletableFuture<Void> cancelOrders(String currencyPair, String side) {
        ExchangeProperties.Exchange exchange = exchangeProperties.getExchange(BITMART.toString());
        String timestamp = String.valueOf(System.currentTimeMillis());
        CancelOrdersRequest request = new CancelOrdersRequest()
                .symbol(currencyPair)
                .side(side);
        String sign = createSha256Signature(timestamp, request, exchange);
        return bitMartExchangeApi.cancelOrders(exchange.getApiKey(), timestamp, sign, request);
    }

    @Override
    public CompletableFuture<Balance> getBalance() {
        return bitMartExchangeApi.getBalance(exchangeProperties.getApiKey(BITMART.toString()))
                .thenApply(getBalanceResponse -> {
                    Balance balance = new Balance();
                    getBalanceResponse.getData().getWallet()
                            .forEach(singleCurrencyResponseData -> {
                                String name = singleCurrencyResponseData.getCurrency();
                                    balance.addCurrency(name, new CurrencyBalance(name,
                                            new BigDecimal(singleCurrencyResponseData.getAvailable())));
                            });
                    return balance;
                });
    }

    private Retrofit createRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(exchangeProperties.getUrl(BITMART.toString()))
                .addConverterFactory(JacksonConverterFactory.create())
                .client(createOkHttpClient())
                .build();
    }

    private OkHttpClient createOkHttpClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .connectTimeout(10L, TimeUnit.SECONDS)
                .readTimeout(20L, TimeUnit.SECONDS)
                .writeTimeout(20L, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();
    }

    private String createSha256Signature(String timestamp, Object request, ExchangeProperties.Exchange exchange) {
        try {
            String paramsString = String.format("%s#%s#%s", timestamp, exchange.getMemo(),
                    objectMapper.writeValueAsString(request));
            Mac sha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(exchange.getApiSecret().getBytes(), "HmacSHA256");
            sha256.init(secretKeySpec);
            return Hex.encodeHexString(sha256.doFinal(paramsString.getBytes()));
        } catch (Exception ex) {
            log.error(ex);
            return null;
        }
    }
}
