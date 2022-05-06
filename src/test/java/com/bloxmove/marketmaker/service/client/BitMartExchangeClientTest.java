package com.bloxmove.marketmaker.service.client;

import com.bitmart.api.Call;
import com.bitmart.api.common.CloudException;
import com.bitmart.api.common.CloudResponse;
import com.bitmart.api.request.account.prv.AccountWalletRequest;
import com.bitmart.api.request.spot.prv.SubmitOrderRequest;
import com.bloxmove.marketmaker.model.Balance;
import com.bloxmove.marketmaker.model.Order;
import com.bloxmove.marketmaker.service.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;

import static com.bloxmove.marketmaker.TestUtils.AMOUNT;
import static com.bloxmove.marketmaker.TestUtils.CURRENCY_PAIR;
import static com.bloxmove.marketmaker.TestUtils.FIRST_CURRENCY;
import static com.bloxmove.marketmaker.TestUtils.PRICE;
import static com.bloxmove.marketmaker.TestUtils.SECOND_CURRENCY;
import static com.bloxmove.marketmaker.TestUtils.SIDE;
import static com.bloxmove.marketmaker.TestUtils.TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class BitMartExchangeClientTest {

    @Autowired
    private BitMartExchangeClient bitMartExchangeClient;
    @MockBean
    private Call bitMartClient;
    @MockBean
    private NotificationRepository notificationRepository;

    private SubmitOrderRequest submitOrderRequest;

    @BeforeEach
    void setUp() throws CloudException {
        String getBalanceResponseContent =
                "{" +
                "\"message\":\"OK\"," +
                "\"code\":1000," +
                "\"trace\":\"408f658d-7756-4ded-ad3b-695a4b5f0246\"," +
                "\"data\":{" +
                    "\"wallet\":[" +
                        "{\"currency\":\"USDT\"," +
                        "\"name\":\"Tether USD\"," +
                        "\"available\":\"52.14249125\"," +
                        "\"frozen\":\"0.00000000\"}," +
                        "{\"currency\":\"BLXM\"," +
                        "\"name\":\"bloXmove Token\"," +
                        "\"available\":\"50.80000000\"," +
                        "\"frozen\":\"0.00000000\"}" +
                        "]" +
                    "}" +
                "}";
        String placeOrderResponseContent =
                "{" +
                "\"message\":\"OK\"," +
                "\"code\":1000," +
                "\"trace\":\"27a97f46-85bd-4c85-a385-d5e74c4c87d6\"," +
                "\"data\":{" +
                    "\"order_id\":29673098058" +
                        "}" +
                "}";
        submitOrderRequest = new SubmitOrderRequest()
                .setSymbol(CURRENCY_PAIR)
                .setSide(SIDE.toString())
                .setType(TYPE.toString())
                .setSize(AMOUNT.toString())
                .setPrice(PRICE.toString());
        when(bitMartClient.callCloud(submitOrderRequest))
                .thenReturn(new CloudResponse().setResponseContent(placeOrderResponseContent));
        when(bitMartClient.callCloud(any(AccountWalletRequest.class)))
                .thenReturn(new CloudResponse().setResponseContent(getBalanceResponseContent));
    }

    @Test
    void shouldReturnOrderWithIdWhenPlacingOrder() {
        Order order = bitMartExchangeClient.placeOrder(
                CURRENCY_PAIR, SIDE.toString(), TYPE.toString(), AMOUNT.toString(), PRICE.toString());
        assertThat(order)
                .hasFieldOrPropertyWithValue("id", 29673098058L);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void shouldReturnEmptyOrderWhenErrorWasThrownDuringPlacingOrder() throws CloudException {
        when(bitMartClient.callCloud(submitOrderRequest)).thenThrow(new IllegalArgumentException());
        Order order = bitMartExchangeClient.placeOrder(
                CURRENCY_PAIR, SIDE.toString(), TYPE.toString(), AMOUNT.toString(), PRICE.toString());
        assertThat(order)
                .hasFieldOrPropertyWithValue("id", null);
        verify(notificationRepository).save(any());
    }

    @Test
    void shouldReturnBalanceWithAmountsWhenGettingBalance() {
        Balance balance = bitMartExchangeClient.getBalance(FIRST_CURRENCY, SECOND_CURRENCY);
        assertThat(balance)
                .hasFieldOrPropertyWithValue("firstCurrencyBalance", new BigDecimal("50.80000000"))
                .hasFieldOrPropertyWithValue("secondCurrencyBalance", new BigDecimal("52.14249125"));
    }

    @Test
    void shouldReturnEmptyBalanceWhenErrorThrownDuringGettingBalance() throws CloudException {
        when(bitMartClient.callCloud(any(AccountWalletRequest.class))).thenThrow(new IllegalArgumentException());
        Balance balance = bitMartExchangeClient.getBalance(FIRST_CURRENCY, SECOND_CURRENCY);
        assertThat(balance)
                .hasFieldOrPropertyWithValue("firstCurrencyBalance", null)
                .hasFieldOrPropertyWithValue("secondCurrencyBalance", null);
    }
}