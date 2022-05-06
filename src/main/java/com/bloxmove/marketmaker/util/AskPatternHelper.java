package com.bloxmove.marketmaker.util;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.RecipientRef;
import akka.actor.typed.javadsl.AskPattern;
import akka.pattern.StatusReply;
import com.bloxmove.marketmaker.actor.Guardian;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * Helps to simplify {@link AskPattern} usage.
 */
@RequiredArgsConstructor
public class AskPatternHelper {

    private final ActorSystem<Guardian.Command> actorSystem;

    public <Req, Resp> Mono<Resp> ask(RecipientRef<? super Req> actorRef, Function<ActorRef<Resp>, Req> reqFactory) {
        CompletionStage<Resp> future = AskPattern.ask(
                actorRef,
                reqFactory::apply,
                Constants.ActorReqTimeouts.LONG,
                actorSystem.scheduler()
        );

        return Mono.fromCompletionStage(future);
    }

    public <Req, Resp> Mono<Resp> askWithStatus(RecipientRef<? super Req> actorRef,
            Function<ActorRef<StatusReply<Resp>>, Req> reqFactory) {

        CompletionStage<Resp> future = AskPattern.askWithStatus(
                actorRef,
                reqFactory::apply,
                Constants.ActorReqTimeouts.LONG,
                actorSystem.scheduler()
        );

        return Mono.fromCompletionStage(future);
    }
}
