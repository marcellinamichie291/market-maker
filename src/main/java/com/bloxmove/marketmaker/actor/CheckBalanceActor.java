package com.bloxmove.marketmaker.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.bloxmove.marketmaker.model.Balance;
import com.bloxmove.marketmaker.model.MarketMakerRequest;
import com.bloxmove.marketmaker.model.Notification;
import com.bloxmove.marketmaker.model.NotificationStatus;
import com.bloxmove.marketmaker.service.MailService;
import com.bloxmove.marketmaker.service.client.ExchangeClient;
import com.bloxmove.marketmaker.service.factory.ExchangeClientFactory;
import com.bloxmove.marketmaker.service.repository.NotificationRepository;
import lombok.ToString;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

import static com.bloxmove.marketmaker.model.NotificationType.LOW_BALANCE;

public class CheckBalanceActor extends AbstractBehavior<CheckBalanceActor.Command> {

    private final ExchangeClientFactory exchangeClientFactory;
    private final MailService mailService;
    private final NotificationRepository notificationRepository;
    private boolean firstCurrencyCheck;
    private boolean secondCurrencyCheck;

    public static Behavior<CheckBalanceActor.Command> create(ExchangeClientFactory exchangeClientFactory,
                                                             MailService mailService,
                                                             NotificationRepository notificationRepository) {
        return Behaviors.logMessages(Behaviors.setup(ctx -> new CheckBalanceActor(ctx, exchangeClientFactory,
                mailService, notificationRepository)));
    }

    private CheckBalanceActor(ActorContext<CheckBalanceActor.Command> ctx,
                              ExchangeClientFactory exchangeClientFactory, MailService mailService,
                              NotificationRepository notificationRepository) {
        super(ctx);
        this.exchangeClientFactory = exchangeClientFactory;
        this.mailService = mailService;
        this.notificationRepository = notificationRepository;
        this.firstCurrencyCheck = true;
        this.secondCurrencyCheck = true;
        ctx.getLog().debug("Created");
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetBalance.class, this::onGetBalanceRequest)
                .onMessage(StopCheckBalanceActor.class, it -> onStopCheckBalanceActor())
                .build();
    }

    private Behavior<CheckBalanceActor.Command> onGetBalanceRequest(GetBalance request) {
        MarketMakerRequest marketMakerRequest = request.getMarketMakerRequest();
        String[] currencies = marketMakerRequest.getCurrencyPair().split("_");
        String firstCurrency = currencies[0];
        String secondCurrency = currencies[1];

        ExchangeClient exchangeClient = exchangeClientFactory.getExchangeClient(marketMakerRequest.getExchangeName());
        Balance balance = exchangeClient.getBalance(firstCurrency, secondCurrency);
        BigDecimal firstCurrencyBalance = balance.getFirstCurrencyBalance();
        BigDecimal secondCurrencyBalance = balance.getSecondCurrencyBalance();

        if (firstCurrencyCheck && firstCurrencyBalance != null && marketMakerRequest.getMinFirstCurrency()
                .compareTo(balance.getFirstCurrencyBalance()) > 0) {
            getContext().getLog().debug("Balance of {} is under minimal required", firstCurrency);
            firstCurrencyCheck = false;
            createNotification(LOW_BALANCE.getMessage(firstCurrency));
        }
        if (secondCurrencyCheck && secondCurrencyBalance != null && marketMakerRequest.getMinSecondCurrency()
                .compareTo(balance.getSecondCurrencyBalance()) > 0) {
            getContext().getLog().debug("Balance of {} is under minimal required", secondCurrency);
            secondCurrencyCheck = false;
            createNotification(LOW_BALANCE.getMessage(secondCurrency));
        }

        if (!firstCurrencyCheck && firstCurrencyBalance != null && marketMakerRequest.getMinFirstCurrency()
                .compareTo(balance.getFirstCurrencyBalance()) < 0) {
            firstCurrencyCheck = true;
        }
        if (!secondCurrencyCheck && secondCurrencyBalance != null && marketMakerRequest.getMinSecondCurrency()
                .compareTo(balance.getSecondCurrencyBalance()) < 0) {
            secondCurrencyCheck = true;
        }
        return Behaviors.same();
    }

    private void createNotification(String message) {
        Notification notification = new Notification()
                .type(LOW_BALANCE)
                .status(NotificationStatus.OPEN)
                .message(message)
                .created(Instant.now());

        mailService.send(message, message);
        notificationRepository.save(notification);
    }

    private Behavior<CheckBalanceActor.Command> onStopCheckBalanceActor() {
        getContext().getLog().debug("CheckBalanceActor stopped");
        return Behaviors.stopped();
    }

    @Value
    public static class GetBalance implements Command {
        private final MarketMakerRequest marketMakerRequest;
    }

    @ToString
    public enum StopCheckBalanceActor implements Command {
        INSTANCE
    }

    public interface Command {
    }
}
