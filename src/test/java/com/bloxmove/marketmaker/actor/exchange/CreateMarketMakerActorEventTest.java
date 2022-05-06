package com.bloxmove.marketmaker.actor.exchange;

import akka.Done;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;
import com.bloxmove.marketmaker.actor.ExchangeActor;
import com.bloxmove.marketmaker.model.Balance;
import com.bloxmove.marketmaker.model.MarketMakerRequest;
import com.bloxmove.marketmaker.model.OrderSide;
import com.bloxmove.marketmaker.model.OrderType;
import com.bloxmove.marketmaker.service.MailService;
import com.bloxmove.marketmaker.service.client.BitMartExchangeClient;
import com.bloxmove.marketmaker.service.factory.ExchangeClientFactory;
import com.bloxmove.marketmaker.service.repository.MarketMakerRepository;
import com.bloxmove.marketmaker.service.repository.NotificationRepository;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.Duration;

import static com.bloxmove.marketmaker.TestUtils.CURRENCY_PAIR;
import static com.bloxmove.marketmaker.TestUtils.EXCHANGE_NAME;
import static com.bloxmove.marketmaker.TestUtils.FIRST_CURRENCY;
import static com.bloxmove.marketmaker.TestUtils.GRID_COUNT;
import static com.bloxmove.marketmaker.TestUtils.SECOND_CURRENCY;
import static com.bloxmove.marketmaker.TestUtils.SINGLE_AMOUNT;
import static com.bloxmove.marketmaker.TestUtils.createMarketMakerRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext
public class CreateMarketMakerActorEventTest {

    @SpyBean
    private ExchangeClientFactory exchangeClientFactory;
    @MockBean
    private MarketMakerRepository marketMakerRepository;
    @MockBean
    private BitMartExchangeClient bitMartExchangeClient;
    @MockBean
    private MailService mailService;
    @MockBean
    private NotificationRepository notificationRepository;
    @ClassRule
    private static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    void shouldTestCreateMarketMakerActorEvent() {
        MarketMakerRequest marketMakerRequest = createMarketMakerRequest();
        TestProbe<ExchangeActor.StopMarketMakerActorResponse> probe = testKit.createTestProbe();
        TestProbe<StatusReply<Done>> doneProbe = testKit.createTestProbe();
        ActorRef<ExchangeActor.Command> actor = testKit.spawn(ExchangeActor.create(
                exchangeClientFactory, marketMakerRepository, mailService, notificationRepository, probe.getRef()));
        when(bitMartExchangeClient.getBalance(FIRST_CURRENCY, SECOND_CURRENCY))
                .thenReturn(new Balance(new BigDecimal(4), new BigDecimal(4)));

        actor.tell(new ExchangeActor.CreateMarketMakerActor(marketMakerRequest, doneProbe.getRef()));

        await().during(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                            verify(exchangeClientFactory, atLeast(2)).getExchangeClient(EXCHANGE_NAME);
                            verify(bitMartExchangeClient, atLeast(1)).getBalance(FIRST_CURRENCY, SECOND_CURRENCY);
                            verify(bitMartExchangeClient, atLeast(GRID_COUNT.intValue())).placeOrder(eq(CURRENCY_PAIR),
                                    eq(OrderSide.BUY.toString()), eq(OrderType.LIMIT.toString()),
                                    eq(SINGLE_AMOUNT.toString()), any());
                            verify(bitMartExchangeClient, atLeast(GRID_COUNT.intValue())).placeOrder(eq(CURRENCY_PAIR),
                                    eq(OrderSide.SELL.toString()), eq(OrderType.LIMIT.toString()),
                                    eq(SINGLE_AMOUNT.toString()), any());
                            verify(marketMakerRepository).save(any());
                            verify(mailService, atLeast(2)).send(any(), any());
                            verify(notificationRepository, atLeast(2)).save(any());
                        }
                );

        StatusReply<Done> message = doneProbe.receiveMessage();
        assertThat(message)
                .hasFieldOrPropertyWithValue("value", Done.done());
    }
}