package com.bloxmove.marketmaker.actor.checkbalance;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.typed.ActorRef;
import com.bloxmove.marketmaker.actor.CheckBalanceActor;
import com.bloxmove.marketmaker.model.Balance;
import com.bloxmove.marketmaker.model.MarketMakerRequest;
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
import java.time.Duration;

import static com.bloxmove.marketmaker.TestUtils.EXCHANGE_NAME;
import static com.bloxmove.marketmaker.TestUtils.FIRST_CURRENCY;
import static com.bloxmove.marketmaker.TestUtils.SECOND_CURRENCY;
import static com.bloxmove.marketmaker.TestUtils.createMarketMakerRequest;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext
public class GetBalanceEventTest {

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
    void shouldTestGetBalanceEvent() {
        ActorRef<CheckBalanceActor.Command> actor = testKit.spawn(CheckBalanceActor.create(exchangeClientFactory,
                mailService, notificationRepository));
        MarketMakerRequest marketMakerRequest = createMarketMakerRequest();
        when(bitMartExchangeClient.getBalance(FIRST_CURRENCY, SECOND_CURRENCY))
                .thenReturn(new Balance(new BigDecimal(4), new BigDecimal(4)));

        actor.tell(new CheckBalanceActor.GetBalance(marketMakerRequest));
        await().during(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    verify(exchangeClientFactory, atLeast(1)).getExchangeClient(EXCHANGE_NAME);
                    verify(bitMartExchangeClient, atLeast(1)).getBalance(FIRST_CURRENCY, SECOND_CURRENCY);
                    verify(mailService, atLeast(2)).send(any(), any());
                    verify(notificationRepository, atLeast(2)).save(any());
                });
    }
}