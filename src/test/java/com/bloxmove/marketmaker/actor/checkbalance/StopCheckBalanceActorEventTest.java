package com.bloxmove.marketmaker.actor.checkbalance;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import com.bloxmove.marketmaker.actor.CheckBalanceActor;
import com.bloxmove.marketmaker.model.Balance;
import com.bloxmove.marketmaker.service.MailService;
import com.bloxmove.marketmaker.service.client.BitMartExchangeClient;
import com.bloxmove.marketmaker.service.factory.ExchangeClientFactory;
import com.bloxmove.marketmaker.service.repository.NotificationRepository;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;

import static com.bloxmove.marketmaker.TestUtils.FIRST_CURRENCY;
import static com.bloxmove.marketmaker.TestUtils.SECOND_CURRENCY;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext
public class StopCheckBalanceActorEventTest {

    @SpyBean
    private ExchangeClientFactory exchangeClientFactory;
    @MockBean
    private BitMartExchangeClient bitMartExchangeClient;
    @MockBean
    private MailService mailService;
    @MockBean
    private NotificationRepository notificationRepository;
    @ClassRule
    private static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    void shouldTestStopCheckBalanceActorEvent() {
        TestProbe<CheckBalanceActor.Command> probe = testKit.createTestProbe();
        ActorRef<CheckBalanceActor.Command> actor = testKit.spawn(CheckBalanceActor.create(exchangeClientFactory,
                mailService, notificationRepository));
        when(bitMartExchangeClient.getBalance(FIRST_CURRENCY, SECOND_CURRENCY))
                .thenReturn(new Balance(new BigDecimal(4), new BigDecimal(4)));

        actor.tell(CheckBalanceActor.StopCheckBalanceActor.INSTANCE);

        probe.expectTerminated(actor);
    }
}