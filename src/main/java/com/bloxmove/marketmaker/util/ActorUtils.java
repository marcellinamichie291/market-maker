package com.bloxmove.marketmaker.util;

import akka.actor.typed.ActorTags;
import reactor.util.function.Tuple2;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Thead safe.
 */
public final class ActorUtils {
    private static final AtomicInteger ID = new AtomicInteger();

    /**
     * Helps with creation of unique actors name.
     */
    public static String createUniqueName(Class clazz) {
        return clazz.getSimpleName() + "-" + ID.incrementAndGet();
    }

    /**
     * Helper method to generate actor tags from array of tuples(key-value pair).
     */
    public static ActorTags prepareTags(Tuple2... tuples) {
        return ActorTags.create(Stream.of(tuples)
                .map(tuple -> tuple.getT1().toString() + "=" + tuple.getT2().toString())
                .collect(Collectors.toSet()));
    }

    private ActorUtils() {
    }
}
