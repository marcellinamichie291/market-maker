package com.bloxmove.marketmaker.util;

import java.math.MathContext;
import java.time.Duration;

import static java.math.RoundingMode.HALF_UP;

public class Constants {

    public static final MathContext SCALE_ROUND = new MathContext(3, HALF_UP);

    /**
     * The idea behind these timeouts is like with microservices timeouts.
     * Split different layers off app and use the biggest timeout on the outer layer to avoid timeouts mess.
     */
    public static class ActorReqTimeouts {
        /**
         * Normally this timeout is applied outside actor system.
         */
        public static final Duration LONG = Duration.ofSeconds(5);
    }
}
