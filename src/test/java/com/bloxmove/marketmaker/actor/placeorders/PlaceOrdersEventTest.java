package com.bloxmove.marketmaker.actor.placeorders;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import com.bloxmove.marketmaker.actor.PlaceOrdersActor;
import com.bloxmove.marketmaker.model.Balance;
import com.bloxmove.marketmaker.model.MarketMakerRequest;
import com.bloxmove.marketmaker.model.OrderSide;
import com.bloxmove.marketmaker.model.OrderType;
import com.bloxmove.marketmaker.service.client.BitMartExchangeClient;
import com.bloxmove.marketmaker.service.factory.ExchangeClientFactory;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;

import static com.bloxmove.marketmaker.TestUtils.CURRENCY_PAIR;
import static com.bloxmove.marketmaker.TestUtils.EXCHANGE_NAME;
import static com.bloxmove.marketmaker.TestUtils.FIRST_CURRENCY;
import static com.bloxmove.marketmaker.TestUtils.GRID_COUNT;
import static com.bloxmove.marketmaker.TestUtils.MAX_PRICE_DELTA;
import static com.bloxmove.marketmaker.TestUtils.MIN_PRICE_DELTA;
import static com.bloxmove.marketmaker.TestUtils.SECOND_CURRENCY;
import static com.bloxmove.marketmaker.TestUtils.SINGLE_AMOUNT;
import static com.bloxmove.marketmaker.TestUtils.createMarketMakerRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext
public class PlaceOrdersEventTest {

    @SpyBean
    private ExchangeClientFactory exchangeClientFactory;
    @MockBean
    private BitMartExchangeClient bitMartExchangeClient;
    @ClassRule
    private static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    void shouldTestPlaceOrdersEvent() {
        TestProbe<PlaceOrdersActor.PlaceOrdersResponse> probe = testKit.createTestProbe();
        ActorRef<PlaceOrdersActor.Command> actor = testKit.spawn(PlaceOrdersActor.create(
                exchangeClientFactory, probe.getRef(), "1"));
        MarketMakerRequest marketMakerRequest = createMarketMakerRequest();
        when(bitMartExchangeClient.getBalance(FIRST_CURRENCY, SECOND_CURRENCY))
                .thenReturn(new Balance(new BigDecimal(4), new BigDecimal(4)));

        actor.tell(new PlaceOrdersActor.PlaceOrders(marketMakerRequest, SINGLE_AMOUNT,
                MAX_PRICE_DELTA, MIN_PRICE_DELTA));

        PlaceOrdersActor.PlaceOrdersResponse placeOrdersResponse = probe.receiveMessage();
        assertThat(placeOrdersResponse)
                .hasFieldOrPropertyWithValue("placeOrderActorId", "1");

        verify(exchangeClientFactory, atLeast(1)).getExchangeClient(EXCHANGE_NAME);
        verify(bitMartExchangeClient, atLeast(GRID_COUNT.intValue())).placeOrder(eq(CURRENCY_PAIR),
                eq(OrderSide.BUY.toString()), eq(OrderType.LIMIT.toString()), eq(SINGLE_AMOUNT.toString()), any());
        verify(bitMartExchangeClient, atLeast(GRID_COUNT.intValue())).placeOrder(eq(CURRENCY_PAIR),
                eq(OrderSide.SELL.toString()), eq(OrderType.LIMIT.toString()), eq(SINGLE_AMOUNT.toString()), any());

        probe.expectTerminated(actor);
    }

}