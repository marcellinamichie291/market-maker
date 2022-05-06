package com.bloxmove.marketmaker.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.bloxmove.marketmaker.service.MailService;
import com.bloxmove.marketmaker.service.factory.ExchangeClientFactory;
import com.bloxmove.marketmaker.service.repository.MarketMakerRepository;
import com.bloxmove.marketmaker.service.ValidationService;
import com.bloxmove.marketmaker.service.repository.NotificationRepository;
import lombok.Value;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;

import static com.bloxmove.marketmaker.util.BehaviorHelper.prepareDefaultManagersBehavior;

public final class Guardian extends AbstractBehavior<Guardian.Command> {

    private final ConfigurableApplicationContext springContext;
    private final ExchangeClientFactory exchangeClientFactory;
    private final ValidationService validationService;
    private final MarketMakerRepository marketMakerRepository;
    private final MailService mailService;
    private final NotificationRepository notificationRepository;
    private final ActorRef<ExchangeManagerActor.Command> exchangeManagerActor;

    public static Behavior<Command> create(ConfigurableApplicationContext springContext,
                                           ExchangeClientFactory exchangeClientFactory,
                                           ValidationService validationService,
                                           MarketMakerRepository marketMakerRepository, MailService mailService,
                                           NotificationRepository notificationRepository) {
        return Behaviors.logMessages(Behaviors.setup(ctx ->
                new Guardian(ctx, springContext, exchangeClientFactory, validationService, marketMakerRepository,
                        mailService, notificationRepository)));
    }

    private Guardian(ActorContext<Command> ctx, ConfigurableApplicationContext springContext,
                     ExchangeClientFactory exchangeClientFactory, ValidationService validationService,
                     MarketMakerRepository marketMakerRepository, MailService mailService,
                     NotificationRepository notificationRepository) {
        super(ctx);
        this.springContext = springContext;
        this.exchangeClientFactory = exchangeClientFactory;
        this.validationService = validationService;
        this.marketMakerRepository = marketMakerRepository;
        this.mailService = mailService;
        this.notificationRepository = notificationRepository;
        this.exchangeManagerActor = spawnExchangeManagerActor();
        ctx.getLog().debug("Created");
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetExchangeManagerActor.class, this::onGetExchangeManagerActor)
                .onSignal(Terminated.class, this::onActorTerminatedSignal)
                .build();
    }

    private Behavior<Command> onGetExchangeManagerActor(GetExchangeManagerActor request) {
        request.getReplyTo().tell(new GetExchangeMangerActorResponse(exchangeManagerActor));

        return Behaviors.same();
    }

    private Behavior<Command> onActorTerminatedSignal(Terminated signal) {
        getContext().getLog().error("One of main actors terminated, stopping app. Info: {}", signal.getRef());

        return Behaviors.stopped();
    }

    private ActorRef<ExchangeManagerActor.Command> spawnExchangeManagerActor() {
        val actorBehavior = ExchangeManagerActor.create(exchangeClientFactory,
                validationService, marketMakerRepository, mailService, notificationRepository);
        val exchangeManagerActor = getContext()
                .spawn(prepareDefaultManagersBehavior(actorBehavior), ExchangeManagerActor.class.getSimpleName());
        getContext().watch(exchangeManagerActor);

        return exchangeManagerActor;
    }

    @Value
    public static class GetExchangeManagerActor implements Command {
        private final ActorRef<GetExchangeMangerActorResponse> replyTo;
    }

    @Value
    public static class GetExchangeMangerActorResponse {
        private final ActorRef<ExchangeManagerActor.Command> exchangeManagerActor;
    }

    public interface Command {
    }
}
