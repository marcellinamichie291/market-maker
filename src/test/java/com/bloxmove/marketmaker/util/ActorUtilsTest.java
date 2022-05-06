package com.bloxmove.marketmaker.util;

import akka.actor.typed.ActorTags;
import org.junit.jupiter.api.Test;
import reactor.util.function.Tuples;

import static com.bloxmove.marketmaker.util.ActorUtils.createUniqueName;
import static com.bloxmove.marketmaker.util.ActorUtils.prepareTags;
import static org.assertj.core.api.Assertions.assertThat;

class ActorUtilsTest {

    @Test
    void shouldCreateUniqueName() {
        String uniqueName = createUniqueName(ActorUtils.class);
        assertThat(uniqueName).contains("ActorUtils-");
    }

    @Test
    void shouldPrepareActorTags() {
        ActorTags actorTags = prepareTags(Tuples.of("CurrencyPair", "BLXM_USDT"));
        assertThat(actorTags.getTags())
                .hasSize(1)
                .first()
                .isEqualTo("CurrencyPair=BLXM_USDT");
    }

}