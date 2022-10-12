package com.bloxmove.marketmaker.actor;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.pattern.StatusReply;
import com.bloxmove.marketmaker.model.ExchangeName;
import com.bloxmove.marketmaker.model.MarketMaker;
import com.bloxmove.marketmaker.model.MarketMakerRequest;
import com.bloxmove.marketmaker.model.MarketMakerShortRequest;
import com.bloxmove.marketmaker.service.MailService;
import com.bloxmove.marketmaker.service.factory.ExchangeClientFactory;
import com.bloxmove.marketmaker.service.mapper.MarketMakerMapper;
import com.bloxmove.marketmaker.service.repository.MarketMakerRepository;
import com.bloxmove.marketmaker.service.repository.NotificationRepository;
import com.bloxmove.marketmaker.util.ActorUtils;
import lombok.Value;
import lombok.val;
import reactor.util.function.Tuples;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.bloxmove.marketmaker.model.MarketMakerStatus.STOPPED;
import static com.bloxmove.marketmaker.model.MarketMakerStatus.WORKING;
import static com.bloxmove.marketmaker.util.ActorUtils.prepareTags;
import static com.bloxmove.marketmaker.util.BehaviorHelper.prepareDefaultManagersBehavior;

public class ExchangeActor extends AbstractBehavior<ExchangeActor.Command> {

    private final ExchangeClientFactory exchangeClientFactory;
    private final MarketMakerRepository marketMakerRepository;
    private final MailService mailService;
    private final NotificationRepository notificationRepository;
    private final Map<String, ActorRef<MarketMakerActor.Command>> marketMakerActors;
    private final ActorRef<StopMarketMakerActorResponse> updatesTo;

    public static Behavior<Command> create(ExchangeClientFactory exchangeClientFactory,
                                           MarketMakerRepository marketMakerRepository, MailService mailService,
                                           NotificationRepository notificationRepository,
                                           ActorRef<StopMarketMakerActorResponse> updatesTo) {
        return Behaviors.logMessages(Behaviors.setup(ctx ->
                new ExchangeActor(ctx, exchangeClientFactory, marketMakerRepository, mailService,
                        notificationRepository, updatesTo)));
    }

    private ExchangeActor(ActorContext<Command> ctx, ExchangeClientFactory exchangeClientFactory,
                          MarketMakerRepository marketMakerRepository, MailService mailService,
                          NotificationRepository notificationRepository,
                          ActorRef<StopMarketMakerActorResponse> updatesTo) {
        super(ctx);
        this.exchangeClientFactory = exchangeClientFactory;
        this.marketMakerRepository = marketMakerRepository;
        this.mailService = mailService;
        this.notificationRepository = notificationRepository;
        this.marketMakerActors = new HashMap<>();
        this.updatesTo = updatesTo;
        ctx.getLog().debug("Created");
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(CreateInitialMarketMakerActor.class, this::onCreateInitialMarketMakerActor)
                .onMessage(CreateMarketMakerActor.class, this::onCreateMarketMakerActor)
                .onMessage(StopMarketMakerActor.class, this::onStopMarketMakerActor)
                .onMessage(StopPlaceOrdersActorsWrappedResp.class, this::onStopPlaceOrdersActorsWrappedResp)
                .build();
    }

    private Behavior<Command> onCreateInitialMarketMakerActor(CreateInitialMarketMakerActor request) {
        MarketMakerRequest marketMakerRequest = request.getMarketMakerRequest();

        spawnMarketMakerActor(marketMakerRequest);

        return Behaviors.same();
    }

    private Behavior<Command> onCreateMarketMakerActor(CreateMarketMakerActor request) {
        MarketMakerRequest marketMakerRequest = request.getMarketMakerRequest();
        String currencyPair = marketMakerRequest.getCurrencyPair();
        MarketMaker marketMaker = MarketMakerMapper.INSTANCE.map(marketMakerRequest);
        if (!marketMakerActors.containsKey(currencyPair)) {
            marketMaker
                    .status(WORKING)
                    .created(Instant.now());
            spawnMarketMakerActor(marketMakerRequest);
        } else {
            MarketMaker marketMakerFromDb = marketMakerRepository.findByCurrencyPairAndStatus(currencyPair, WORKING);
            marketMaker
                    .id(marketMakerFromDb.getId())
                    .status(marketMakerFromDb.getStatus())
                    .created(marketMakerFromDb.getCreated())
                    .updated(Instant.now());
            ActorRef<MarketMakerActor.Command> marketMakerActor = marketMakerActors.get(currencyPair);
            marketMakerActor.tell(new MarketMakerActor.InitializeMarketMaker(marketMakerRequest));
        }

        marketMakerRepository.save(marketMaker);
        request.getReplyTo().tell(StatusReply.success(Done.done()));

        return Behaviors.same();
    }

    private Behavior<Command> onStopMarketMakerActor(StopMarketMakerActor request) {
        MarketMakerShortRequest marketMakerShortRequest = request.getMarketMakerShortRequest();
        String currencyPair = marketMakerShortRequest.getCurrencyPair();
        if (!marketMakerActors.containsKey(currencyPair)) {
            request.getReplyTo().tell(StatusReply.error(
                    String.format("Market maker for currency pair %s does not exist", currencyPair)));
        }

        MarketMaker marketMaker = marketMakerRepository.findByCurrencyPairAndStatus(currencyPair, WORKING)
                .status(STOPPED)
                .updated(Instant.now());
        marketMakerRepository.save(marketMaker);

        request.getReplyTo().tell(StatusReply.success(Done.done()));
        ActorRef<MarketMakerActor.Command> marketMakerActor = marketMakerActors.get(currencyPair);
        marketMakerActor.tell(MarketMakerActor.StopMarketMaker.INSTANCE);
        marketMakerActors.remove(currencyPair);

        return Behaviors.same();
    }

    private Behavior<Command> onStopPlaceOrdersActorsWrappedResp(StopPlaceOrdersActorsWrappedResp resp) {
        MarketMakerRequest marketMakerRequest = resp.getResponse().getMarketMakerRequest();
        if (marketMakerActors.isEmpty()) {
            updatesTo.tell(new StopMarketMakerActorResponse(marketMakerRequest.getExchangeName()));
            getContext().getLog().debug("There is no market makers for exchange {}. ExchangeActor stopped",
                    marketMakerRequest.getExchangeName());
            return Behaviors.stopped();
        }

        return Behaviors.same();
    }

    private void spawnMarketMakerActor(MarketMakerRequest marketMakerRequest) {
        String currencyPair = marketMakerRequest.getCurrencyPair();
        getContext().getLog().debug("Creating MarketMakerActor with currencyPair {}", currencyPair);

        val adapter = getContext().messageAdapter(
                MarketMakerActor.StopPlaceOrdersActorsResponse.class,
                StopPlaceOrdersActorsWrappedResp::new);
        val actorBehavior = MarketMakerActor.create(
                exchangeClientFactory, mailService, notificationRepository, adapter, marketMakerRequest);

        val marketMakerActor = getContext().spawn(
                prepareDefaultManagersBehavior(actorBehavior),
                ActorUtils.createUniqueName(MarketMakerActor.class),
                prepareTags(Tuples.of("CurrencyPair", currencyPair)));

        marketMakerActors.put(currencyPair, marketMakerActor);
        marketMakerActor.tell(new MarketMakerActor.InitializeMarketMaker(marketMakerRequest));
    }

    @Value
    public static class CreateInitialMarketMakerActor implements Command {
        private final MarketMakerRequest marketMakerRequest;
    }

    @Value
    public static class CreateMarketMakerActor implements Command {
        private final MarketMakerRequest marketMakerRequest;
        private final ActorRef<StatusReply<Done>> replyTo;
    }

    @Value
    public static class StopMarketMakerActor implements Command {
        private final MarketMakerShortRequest marketMakerShortRequest;
        private final ActorRef<StatusReply<Done>> replyTo;
    }

    @Value
    public static class StopPlaceOrdersActorsWrappedResp implements Command {
        private final MarketMakerActor.StopPlaceOrdersActorsResponse response;
    }

    @Value
    public static class StopMarketMakerActorResponse {
        private final ExchangeName exchangeName;
    }

    public interface Command {
    }
}
