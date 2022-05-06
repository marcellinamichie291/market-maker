package com.bloxmove.marketmaker.util;

import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.internal.adapter.ActorAdapter;
import akka.actor.typed.javadsl.Behaviors;
import lombok.val;

public class BehaviorHelper {

    public static <T> Behavior<T> prepareDefaultManagersBehavior(Behavior<T> actorBehavior) {
        val stopOnTypedActorErrorBehavior = Behaviors.supervise(actorBehavior)
                .onFailure(ActorAdapter.TypedActorFailedException.class, SupervisorStrategy.stop());

        return Behaviors.supervise(stopOnTypedActorErrorBehavior)
                .onFailure(SupervisorStrategy.resume());
    }

    private BehaviorHelper() {
    }
}
