package com.bloxmove.marketmaker.service.client;

import com.bitmart.api.Call;
import com.bitmart.api.common.CloudResponse;
import com.bitmart.api.request.account.prv.AccountWalletRequest;
import com.bitmart.api.request.spot.prv.SubmitOrderRequest;
import com.bloxmove.marketmaker.model.Balance;
import com.bloxmove.marketmaker.model.Notification;
import com.bloxmove.marketmaker.model.NotificationStatus;
import com.bloxmove.marketmaker.model.NotificationType;
import com.bloxmove.marketmaker.model.Order;
import com.bloxmove.marketmaker.model.bitmart.GetBalanceResponse;
import com.bloxmove.marketmaker.model.bitmart.PlaceOrderResponse;
import com.bloxmove.marketmaker.service.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

import static com.bloxmove.marketmaker.model.NotificationType.PLACE_ORDER_FAIL;

@Component
@RequiredArgsConstructor
@Log4j2
public class BitMartExchangeClient implements ExchangeClient {

    private final Call bitMartClient;
    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;

    @Override
    public Order placeOrder(String currencyPair, String side, String type, String size, String price) {
        SubmitOrderRequest request = new SubmitOrderRequest()
                .setSymbol(currencyPair)
                .setSide(side)
                .setType(type)
                .setSize(size)
                .setPrice(price);
        log.info("Placing order {}", request);
        //todo make log.debug instead
        try {
            CloudResponse cloudResponse = bitMartClient.callCloud(request);
            PlaceOrderResponse placeOrderResponse = objectMapper.readValue(
                    cloudResponse.getResponseContent(), PlaceOrderResponse.class);
            return new Order(placeOrderResponse.getData().getOrderId());
        } catch (Exception ex) {
            log.error(ex);
            Notification notification = new Notification()
                    .type(PLACE_ORDER_FAIL)
                    .status(NotificationStatus.OPEN)
                    .message(PLACE_ORDER_FAIL.getMessage(request.toString()))
                    .created(Instant.now());
            notificationRepository.save(notification);
            //todo move to placeOrdersActor after providing own client implementation
            return new Order();
        }
    }

    @Override
    public Balance getBalance(String firstCurrency, String secondCurrency) {
        AccountWalletRequest request = new AccountWalletRequest();
        try {
            Balance balance = new Balance();
            CloudResponse cloudResponse = bitMartClient.callCloud(request);
            GetBalanceResponse getBalanceResponse = objectMapper.readValue(
                    cloudResponse.getResponseContent(), GetBalanceResponse.class);
            log.info("Got balances response: {}", getBalanceResponse.getData());
            //todo make log.debug instead
            getBalanceResponse.getData().getWallet()
                    .forEach(singleCurrencyResponseData -> {
                        if(firstCurrency.equals(singleCurrencyResponseData.getCurrency())) {
                            balance.setFirstCurrencyBalance(new BigDecimal(singleCurrencyResponseData.getAvailable()));
                        }
                        if(secondCurrency.equals(singleCurrencyResponseData.getCurrency())) {
                            balance.setSecondCurrencyBalance(new BigDecimal(singleCurrencyResponseData.getAvailable()));
                        }
                    });
            return balance;
        } catch (Exception ex) {
            log.error(ex);
            return new Balance();
        }
    }
}
