package com.bloxmove.marketmaker.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.bloxmove.marketmaker.model.MarketMakerRequest;
import com.bloxmove.marketmaker.service.MailService;
import com.bloxmove.marketmaker.service.factory.ExchangeClientFactory;
import com.bloxmove.marketmaker.service.repository.NotificationRepository;
import com.bloxmove.marketmaker.util.ActorUtils;
import lombok.ToString;
import lombok.Value;
import lombok.val;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.bloxmove.marketmaker.util.ActorUtils.prepareTags;
import static com.bloxmove.marketmaker.util.BehaviorHelper.prepareDefaultManagersBehavior;
import static com.bloxmove.marketmaker.util.Constants.SCALE_ROUND;

public class MarketMakerActor extends AbstractBehavior<MarketMakerActor.Command> {

    private final ExchangeClientFactory exchangeClientFactory;
    private final MailService mailService;
    private final NotificationRepository notificationRepository;
    private final MarketMakerRequest marketMakerRequest;
    private final BigDecimal singleAmount;
    private final BigDecimal maxPriceDelta;
    private final BigDecimal minPriceDelta;
    private final Map<String, ActorRef<PlaceOrdersActor.Command>> placeOrderActors;
    private final ActorRef<CheckBalanceActor.Command> checkBalanceActor;

    public static Behavior<MarketMakerActor.Command> create(ExchangeClientFactory exchangeClientFactory,
                                                            MailService mailService,
                                                            NotificationRepository notificationRepository,
                                                            MarketMakerRequest marketMakerRequest) {
        return Behaviors.logMessages(Behaviors.withTimers(timer -> Behaviors.setup(ctx -> {

                    ctx.getSelf().tell(CreatePlaceOrdersActor.INSTANCE);
                    timer.startTimerWithFixedDelay(CreatePlaceOrdersActor.INSTANCE,
                            Duration.ofSeconds(marketMakerRequest.getDelay()));

                    return new MarketMakerActor(ctx, exchangeClientFactory, mailService, notificationRepository,
                            marketMakerRequest);
                })
        ));
    }

    private MarketMakerActor(ActorContext<Command> ctx, ExchangeClientFactory exchangeClientFactory,
                             MailService mailService, NotificationRepository notificationRepository,
                             MarketMakerRequest marketMakerRequest) {
        super(ctx);
        this.exchangeClientFactory = exchangeClientFactory;
        this.mailService = mailService;
        this.notificationRepository = notificationRepository;
        this.marketMakerRequest = marketMakerRequest;
        this.placeOrderActors = new HashMap<>();
        this.checkBalanceActor = spawnBalanceActor();
        ctx.getLog().debug("Created");

        BigDecimal gridCount = BigDecimal.valueOf(marketMakerRequest.getGridCount());
        BigDecimal maxTargetDelta = marketMakerRequest.getMaxPrice().subtract(
                marketMakerRequest.getTargetPrice(), SCALE_ROUND);
        BigDecimal minTargetDelta = marketMakerRequest.getTargetPrice().subtract(
                marketMakerRequest.getMinPrice(), SCALE_ROUND);
        this.singleAmount = marketMakerRequest.getAmount().divide(gridCount, SCALE_ROUND)
                .divide(BigDecimal.valueOf(2), SCALE_ROUND);
        this.maxPriceDelta = maxTargetDelta.divide(gridCount, SCALE_ROUND);
        this.minPriceDelta = minTargetDelta.divide(gridCount, SCALE_ROUND);
    }

    @Override
    public Receive<Command> createReceive() {
        return workingBehavior();
    }

    private Receive<Command> workingBehavior() {
        return newReceiveBuilder()
                .onMessage(CreatePlaceOrdersActor.class, it -> onCreatePlaceOrdersActorRequest())
                .onMessage(PlaceOrdersWrappedResp.class, this::onPlaceOrdersResp)
                .onMessage(StopPlaceOrdersActors.class, it -> onStopPlaceOrdersActorsRequest())
                .build();
    }

    private Receive<Command> stoppingBehavior() {
        return newReceiveBuilder()
                .onMessage(CreatePlaceOrdersActor.class, it -> onCreatePlaceOrdersRequestWhileStopping())
                .onMessage(PlaceOrdersWrappedResp.class, this::onPlaceOrdersResp)
                .build();
    }

    private Behavior<Command> onCreatePlaceOrdersActorRequest() {
        getContext().getLog().debug("Creating PlaceOrdersActor for {}", marketMakerRequest);
        spawnPlaceOrdersActor();
        checkBalanceActor.tell(new CheckBalanceActor.GetBalance(marketMakerRequest));
        return Behaviors.same();
    }

    private Behavior<Command> onPlaceOrdersResp(PlaceOrdersWrappedResp resp) {
        placeOrderActors.remove(resp.getResponse().getPlaceOrderActorId());
        return Behaviors.same();
    }

    private Behavior<Command> onStopPlaceOrdersActorsRequest() {
        checkBalanceActor.tell(CheckBalanceActor.StopCheckBalanceActor.INSTANCE);
        return stoppingBehavior();
    }

    private Behavior<Command> onCreatePlaceOrdersRequestWhileStopping() {
        if (placeOrderActors.isEmpty()) {
            getContext().getLog().debug("MarketMakerActor for currencyPair {} stopped",
                    marketMakerRequest.getCurrencyPair());
            return Behaviors.stopped();
        }
        return Behaviors.same();
    }

    private void spawnPlaceOrdersActor() {
        String placeOrdersActorId = UUID.randomUUID().toString();
        getContext().getLog().debug("Creating PlaceOrdersActor with id {}", placeOrdersActorId);

        val adapter = getContext().messageAdapter(
                PlaceOrdersActor.PlaceOrdersResponse.class, PlaceOrdersWrappedResp::new);
        val actorBehavior = PlaceOrdersActor.create(
                exchangeClientFactory, adapter, placeOrdersActorId);

        val placeOrdersActor = getContext().spawn(
                prepareDefaultManagersBehavior(actorBehavior),
                ActorUtils.createUniqueName(PlaceOrdersActor.class),
                prepareTags(Tuples.of("PlaceOrdersActorId", placeOrdersActorId)));

        placeOrderActors.put(placeOrdersActorId, placeOrdersActor);

        placeOrdersActor.tell(new PlaceOrdersActor.PlaceOrders(
                marketMakerRequest, singleAmount, maxPriceDelta, minPriceDelta));
    }

    private ActorRef<CheckBalanceActor.Command> spawnBalanceActor() {
        val actorBehavior = CheckBalanceActor.create(
                exchangeClientFactory, mailService, notificationRepository);
        val checkBalanceActor = getContext().spawn(
                prepareDefaultManagersBehavior(actorBehavior),
                ActorUtils.createUniqueName(CheckBalanceActor.class));

        return checkBalanceActor;
    }

    @ToString
    public enum CreatePlaceOrdersActor implements Command {
        INSTANCE
    }

    @ToString
    public enum StopPlaceOrdersActors implements Command {
        INSTANCE
    }

    @Value
    public static class PlaceOrdersWrappedResp implements Command {
        private final PlaceOrdersActor.PlaceOrdersResponse response;
    }

    public interface Command {
    }
}
