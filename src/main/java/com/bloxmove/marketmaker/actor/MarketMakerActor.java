package com.bloxmove.marketmaker.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import com.bloxmove.marketmaker.model.MarketMakerRequest;
import com.bloxmove.marketmaker.service.MailService;
import com.bloxmove.marketmaker.service.factory.ExchangeClientFactory;
import com.bloxmove.marketmaker.service.repository.NotificationRepository;
import com.bloxmove.marketmaker.util.ActorUtils;
import lombok.ToString;
import lombok.Value;
import lombok.val;

import java.time.Duration;

import static com.bloxmove.marketmaker.util.BehaviorHelper.prepareDefaultManagersBehavior;

public class MarketMakerActor extends AbstractBehavior<MarketMakerActor.Command> {

    private final TimerScheduler<Command> timer;
    private final ExchangeClientFactory exchangeClientFactory;
    private final MailService mailService;
    private final NotificationRepository notificationRepository;
    private final ActorRef<StopPlaceOrdersActorsResponse> updatesTo;
    private MarketMakerRequest marketMakerRequest;
    private final ActorRef<CheckBalanceActor.Command> checkBalanceActor;
    private final ActorRef<CancelOrdersActor.Command> cancelOrdersActor;

    public static Behavior<MarketMakerActor.Command> create(ExchangeClientFactory exchangeClientFactory,
                                                            MailService mailService,
                                                            NotificationRepository notificationRepository,
                                                            ActorRef<StopPlaceOrdersActorsResponse> updatesTo,
                                                            MarketMakerRequest marketMakerRequest) {
        return Behaviors.logMessages(Behaviors.withTimers(timer -> Behaviors.setup(ctx ->
                new MarketMakerActor(ctx, timer, exchangeClientFactory, mailService, notificationRepository,
                        updatesTo, marketMakerRequest))
        ));
    }

    private MarketMakerActor(ActorContext<Command> ctx, TimerScheduler<Command> timer,
                             ExchangeClientFactory exchangeClientFactory, MailService mailService,
                             NotificationRepository notificationRepository,
                             ActorRef<StopPlaceOrdersActorsResponse> updatesTo,
                             MarketMakerRequest marketMakerRequest) {
        super(ctx);
        this.timer = timer;
        this.exchangeClientFactory = exchangeClientFactory;
        this.mailService = mailService;
        this.notificationRepository = notificationRepository;
        this.updatesTo = updatesTo;
        this.marketMakerRequest = marketMakerRequest;
        this.checkBalanceActor = spawnBalanceActor();
        this.cancelOrdersActor = spawnCancelOrdersActor();
        ctx.getLog().debug("Created");
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(InitializeMarketMaker.class, this::onInitializeMarketMaker)
                .onMessage(SingleExecution.class, it -> onSingleExecution())
                .onMessage(CancelOrdersWrappedResp.class, this::onCancelOrdersResp)
                .onMessage(StopMarketMaker.class, it -> onStopMarketMaker())
                .build();
    }

    public Receive<Command> stoppingBehavior() {
        return newReceiveBuilder()
                .onMessage(InitializeMarketMaker.class, this::onInitializeMarketMakerWhileStopping)
                .onMessage(SingleExecution.class, it -> onSingleExecutionWhileStopping())
                .onMessage(CancelOrdersWrappedResp.class, this::onCancelOrdersRespWhileStopping)
                .onMessage(StopMarketMaker.class, it -> onStopMarketMakerWhileStopping())
                .build();
    }

    private Behavior<Command> onInitializeMarketMaker(InitializeMarketMaker req) {
        marketMakerRequest = req.getMarketMakerRequest();
        getContext().getSelf().tell(SingleExecution.INSTANCE);
        timer.startTimerWithFixedDelay(SingleExecution.INSTANCE, Duration.ofSeconds(marketMakerRequest.getDelay()));
        return Behaviors.same();
    }

    private Behavior<Command> onSingleExecution() {
        checkBalanceActor.tell(new CheckBalanceActor.GetBalance(marketMakerRequest));
        cancelOrdersActor.tell(CancelOrdersActor.CancelOrders.INSTANCE);
        return Behaviors.same();
    }

    private Behavior<Command> onCancelOrdersResp(CancelOrdersWrappedResp resp) {
        spawnPlaceOrdersActor();
        return Behaviors.same();
    }

    private Behavior<Command> onStopMarketMaker() {
        cancelOrdersActor.tell(CancelOrdersActor.CancelOrders.INSTANCE);
        return stoppingBehavior();
    }

    private Behavior<Command> onInitializeMarketMakerWhileStopping(InitializeMarketMaker req) {
        return Behaviors.same();
    }

    private Behavior<Command> onSingleExecutionWhileStopping() {
        return Behaviors.same();
    }

    private Behavior<Command> onCancelOrdersRespWhileStopping(CancelOrdersWrappedResp resp) {
        getContext().getLog().debug("MarketMakerActor for currencyPair {} stopped",
                marketMakerRequest.getCurrencyPair());
        updatesTo.tell(new StopPlaceOrdersActorsResponse(marketMakerRequest));
        return Behaviors.stopped();
    }

    private Behavior<Command> onStopMarketMakerWhileStopping() {
        return Behaviors.same();
    }

    private ActorRef<CheckBalanceActor.Command> spawnBalanceActor() {
        val actorBehavior = CheckBalanceActor.create(
                exchangeClientFactory, mailService, notificationRepository);
        val checkBalanceActor = getContext().spawn(
                prepareDefaultManagersBehavior(actorBehavior),
                ActorUtils.createUniqueName(CheckBalanceActor.class));

        return checkBalanceActor;
    }

    private ActorRef<CancelOrdersActor.Command> spawnCancelOrdersActor() {
        val adapter = getContext().messageAdapter(
                CancelOrdersActor.CancelOrdersResponse.class, CancelOrdersWrappedResp::new);
        val actorBehavior = CancelOrdersActor.create(
                exchangeClientFactory, notificationRepository, adapter, marketMakerRequest);

        val cancelOrdersActor = getContext().spawn(
                prepareDefaultManagersBehavior(actorBehavior),
                ActorUtils.createUniqueName(PlaceOrdersActor.class));

        return cancelOrdersActor;
    }

    private void spawnPlaceOrdersActor() {
        val actorBehavior = PlaceOrdersActor.create(exchangeClientFactory, notificationRepository,
                marketMakerRequest);
        val placeOrdersActor = getContext().spawn(
                prepareDefaultManagersBehavior(actorBehavior),
                ActorUtils.createUniqueName(PlaceOrdersActor.class));

        placeOrdersActor.tell(PlaceOrdersActor.PlaceOrders.INSTANCE);
    }

    @Value
    public static class InitializeMarketMaker implements Command {
        private final MarketMakerRequest marketMakerRequest;
    }

    @ToString
    private enum SingleExecution implements Command {
        INSTANCE
    }

    @Value
    public static class CancelOrdersWrappedResp implements Command {
        private final CancelOrdersActor.CancelOrdersResponse response;
    }

    @ToString
    public enum StopMarketMaker implements Command {
        INSTANCE
    }

    @Value
    public static class StopPlaceOrdersActorsResponse {
        private final MarketMakerRequest marketMakerRequest;
    }

    public interface Command {
    }
}
