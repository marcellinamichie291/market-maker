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
import com.bloxmove.marketmaker.model.MarketMakerRequest;
import com.bloxmove.marketmaker.model.MarketMakerShortRequest;
import com.bloxmove.marketmaker.model.MarketMakerStatus;
import com.bloxmove.marketmaker.service.MailService;
import com.bloxmove.marketmaker.service.factory.ExchangeClientFactory;
import com.bloxmove.marketmaker.service.mapper.MarketMakerMapper;
import com.bloxmove.marketmaker.service.repository.MarketMakerRepository;
import com.bloxmove.marketmaker.service.ValidationService;
import com.bloxmove.marketmaker.service.repository.NotificationRepository;
import com.bloxmove.marketmaker.util.ActorUtils;
import lombok.ToString;
import lombok.Value;
import lombok.val;
import reactor.util.function.Tuples;

import java.util.HashMap;
import java.util.Map;

import static com.bloxmove.marketmaker.util.ActorUtils.prepareTags;
import static com.bloxmove.marketmaker.util.BehaviorHelper.prepareDefaultManagersBehavior;

public class ExchangeManagerActor extends AbstractBehavior<ExchangeManagerActor.Command> {

    private final ExchangeClientFactory exchangeClientFactory;
    private final ValidationService validationService;
    private final MarketMakerRepository marketMakerRepository;
    private final MailService mailService;
    private final NotificationRepository notificationRepository;
    private final Map<ExchangeName, ActorRef<ExchangeActor.Command>> exchangeActors;

    public static Behavior<Command> create(ExchangeClientFactory exchangeClientFactory,
                                           ValidationService validationService,
                                           MarketMakerRepository marketMakerRepository,
                                           MailService mailService, NotificationRepository notificationRepository) {
        return Behaviors.logMessages(Behaviors.setup(ctx -> new ExchangeManagerActor(ctx, exchangeClientFactory,
                validationService, marketMakerRepository, mailService, notificationRepository)));
    }

    private ExchangeManagerActor(ActorContext<Command> ctx, ExchangeClientFactory exchangeClientFactory,
                                 ValidationService validationService, MarketMakerRepository marketMakerRepository,
                                 MailService mailService, NotificationRepository notificationRepository) {
        super(ctx);
        this.exchangeClientFactory = exchangeClientFactory;
        this.validationService = validationService;
        this.marketMakerRepository = marketMakerRepository;
        this.mailService = mailService;
        this.notificationRepository = notificationRepository;
        this.exchangeActors = new HashMap<>();
        ctx.getSelf().tell(GetMarketMakers.INSTANCE);
        ctx.getLog().debug("Created");
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetMarketMakers.class, it -> onGetMarketMakersRequest())
                .onMessage(CreateInitialExchangeActor.class, this::onCreateInitialExchangeActorRequest)
                .onMessage(CreateExchangeActor.class, this::onCreateExchangeActorRequest)
                .onMessage(StopExchangeActor.class, this::onStopExchangeActorRequest)
                .onMessage(StopExchangeActorWrappedResp.class, this::onStopExchangeActorWrappedResp)
                .build();
    }

    private Behavior<Command> onGetMarketMakersRequest() {
        marketMakerRepository.findByStatus(MarketMakerStatus.WORKING).stream()
                .map(MarketMakerMapper.INSTANCE::map)
                .forEach(request -> getContext().getSelf().tell(new CreateInitialExchangeActor(request)));
        return Behaviors.same();
    }

    private Behavior<Command> onCreateInitialExchangeActorRequest(CreateInitialExchangeActor request) {
        MarketMakerRequest marketMakerRequest = request.getMarketMakerRequest();
        ActorRef<ExchangeActor.Command> exchangeActor = getExchangeActor(marketMakerRequest);

        exchangeActor.tell(new ExchangeActor.CreateInitialMarketMakerActor(marketMakerRequest));

        return Behaviors.same();
    }

    private Behavior<Command> onCreateExchangeActorRequest(CreateExchangeActor request) {
        MarketMakerRequest marketMakerRequest = request.getMarketMakerRequest();
        validate(marketMakerRequest, request.getReplyTo());
        ActorRef<ExchangeActor.Command> exchangeActor = getExchangeActor(marketMakerRequest);

        exchangeActor.tell(new ExchangeActor.CreateMarketMakerActor(marketMakerRequest, request.getReplyTo()));

        return Behaviors.same();
    }

    private Behavior<Command> onStopExchangeActorRequest(StopExchangeActor request) {
        MarketMakerShortRequest marketMakerShortRequest = request.getMarketMakerShortRequest();
        ExchangeName exchangeName = marketMakerShortRequest.getExchangeName();
        if (!exchangeActors.containsKey(exchangeName)) {
            request.getReplyTo().tell(StatusReply.error(
                    String.format("Exchange with name %s does not exist", exchangeName)));
        }
        ActorRef<ExchangeActor.Command> exchangeActor = exchangeActors.get(exchangeName);

        exchangeActor.tell(new ExchangeActor.StopMarketMakerActor(marketMakerShortRequest, request.getReplyTo()));

        return Behaviors.same();
    }

    private Behavior<Command> onStopExchangeActorWrappedResp(StopExchangeActorWrappedResp resp) {
        exchangeActors.remove(resp.getResponse().getExchangeName());
        return Behaviors.same();
    }

    private ActorRef<ExchangeActor.Command> getExchangeActor(MarketMakerRequest marketMakerRequest) {
        ExchangeName exchangeName = marketMakerRequest.getExchangeName();
        if (exchangeActors.containsKey(exchangeName)) {
            return exchangeActors.get(exchangeName);
        }
        return spawnExchangeActor(exchangeName);
    }

    private ActorRef<ExchangeActor.Command> spawnExchangeActor(ExchangeName exchangeName) {
        getContext().getLog().debug("Creating ExchangeActor with exchangeName {}", exchangeName);
        val adapter = getContext().messageAdapter(
                ExchangeActor.StopMarketMakerActorResponse.class, StopExchangeActorWrappedResp::new);
        val actorBehavior = ExchangeActor.create(exchangeClientFactory,
                marketMakerRepository, mailService, notificationRepository, adapter);

        val exchangeActor = getContext().spawn(
                prepareDefaultManagersBehavior(actorBehavior),
                ActorUtils.createUniqueName(ExchangeActor.class),
                prepareTags(Tuples.of("ExchangeName", exchangeName)));

        exchangeActors.put(exchangeName, exchangeActor);
        return exchangeActor;
    }

    private void validate(MarketMakerRequest marketMakerRequest, ActorRef<StatusReply<Done>> replyTo) {
        try {
            validationService.validate(marketMakerRequest);
        } catch (Exception ex) {
            replyTo.tell(StatusReply.error(ex));
        }
    }

    @ToString
    public enum GetMarketMakers implements Command {
        INSTANCE
    }

    @Value
    public static class CreateInitialExchangeActor implements Command {
        private final MarketMakerRequest marketMakerRequest;
    }

    @Value
    public static class CreateExchangeActor implements Command {
        private final MarketMakerRequest marketMakerRequest;
        private final ActorRef<StatusReply<Done>> replyTo;
    }

    @Value
    public static class StopExchangeActor implements Command {
        private final MarketMakerShortRequest marketMakerShortRequest;
        private final ActorRef<StatusReply<Done>> replyTo;
    }

    @Value
    public static class StopExchangeActorWrappedResp implements Command {
        private final ExchangeActor.StopMarketMakerActorResponse response;
    }

    public interface Command {
    }
}
