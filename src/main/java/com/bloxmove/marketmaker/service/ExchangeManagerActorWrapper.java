package com.bloxmove.marketmaker.service;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;
import com.bloxmove.marketmaker.actor.ExchangeManagerActor;
import com.bloxmove.marketmaker.model.MarketMakerRequest;
import com.bloxmove.marketmaker.model.MarketMakerShortRequest;
import com.bloxmove.marketmaker.util.AskPatternHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RequiredArgsConstructor
public class ExchangeManagerActorWrapper {

    private final ActorRef<ExchangeManagerActor.Command> managerActor;
    private final AskPatternHelper askPatternHelper;

    public Mono<Void> createMarketMaker(MarketMakerRequest request) {
        return askPatternHelper.askWithStatus(
                managerActor,
                (ActorRef<StatusReply<Done>> replyTo) ->
                        new ExchangeManagerActor.CreateExchangeActor(request, replyTo))
                .onErrorMap(error -> new ResponseStatusException(INTERNAL_SERVER_ERROR, error.getMessage(), error))
                .then();
    }

    public Mono<Void> stopMarketMaker(MarketMakerShortRequest request) {
        return askPatternHelper.askWithStatus(
                managerActor,
                (ActorRef<StatusReply<Done>> replyTo) ->
                        new ExchangeManagerActor.StopExchangeActor(request, replyTo))
                .onErrorMap(error -> new ResponseStatusException(INTERNAL_SERVER_ERROR, error.getMessage(), error))
                .then();
    }
}
