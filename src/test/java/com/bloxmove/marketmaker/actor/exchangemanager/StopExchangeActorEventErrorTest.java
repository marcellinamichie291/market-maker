package com.bloxmove.marketmaker.actor.exchangemanager;

import akka.Done;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;
import com.bloxmove.marketmaker.actor.ExchangeManagerActor;
import com.bloxmove.marketmaker.model.Balance;
import com.bloxmove.marketmaker.model.MarketMakerShortRequest;
import com.bloxmove.marketmaker.service.MailService;
import com.bloxmove.marketmaker.service.ValidationService;
import com.bloxmove.marketmaker.service.client.BitMartExchangeClient;
import com.bloxmove.marketmaker.service.factory.ExchangeClientFactory;
import com.bloxmove.marketmaker.service.repository.MarketMakerRepository;
import com.bloxmove.marketmaker.service.repository.NotificationRepository;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;

import static com.bloxmove.marketmaker.TestUtils.FIRST_CURRENCY;
import static com.bloxmove.marketmaker.TestUtils.SECOND_CURRENCY;
import static com.bloxmove.marketmaker.TestUtils.createMarketMakerShortRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext
public class StopExchangeActorEventErrorTest {

    @Autowired
    private ValidationService validationService;
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
    void shouldTestDeleteExchangeActorEventError() {
        MarketMakerShortRequest marketMakershortRequest = createMarketMakerShortRequest();
        TestProbe<StatusReply<Done>> doneProbe = testKit.createTestProbe();
        ActorRef<ExchangeManagerActor.Command> actor = testKit.spawn(ExchangeManagerActor.create(
                exchangeClientFactory, validationService, marketMakerRepository, mailService, notificationRepository));
        when(bitMartExchangeClient.getBalance(FIRST_CURRENCY, SECOND_CURRENCY))
                .thenReturn(new Balance(new BigDecimal(4), new BigDecimal(4)));

        actor.tell(new ExchangeManagerActor.StopExchangeActor(marketMakershortRequest, doneProbe.getRef()));


        StatusReply<Done> message = doneProbe.receiveMessage();
        assertThat(message.getError())
                .hasFieldOrPropertyWithValue("message", "Exchange with name bitmart does not exist");
    }
}