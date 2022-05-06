package com.bloxmove.marketmaker.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.bloxmove.marketmaker.model.MarketMakerRequest;
import com.bloxmove.marketmaker.model.OrderSide;
import com.bloxmove.marketmaker.model.OrderType;
import com.bloxmove.marketmaker.service.client.ExchangeClient;
import com.bloxmove.marketmaker.service.factory.ExchangeClientFactory;
import lombok.Value;

import java.math.BigDecimal;

import static com.bloxmove.marketmaker.util.Constants.SCALE_ROUND;

public class PlaceOrdersActor extends AbstractBehavior<PlaceOrdersActor.Command> {

    private final ExchangeClientFactory exchangeClientFactory;
    private final ActorRef<PlaceOrdersResponse> updatesTo;
    private final String placeOrderActorId;

    public static Behavior<PlaceOrdersActor.Command> create(ExchangeClientFactory exchangeClientFactory,
                                                            ActorRef<PlaceOrdersResponse> updatesTo,
                                                            String placeOrderActorId) {
        return Behaviors.logMessages(Behaviors.setup(ctx ->
                new PlaceOrdersActor(ctx, exchangeClientFactory, updatesTo, placeOrderActorId)));
    }

    private PlaceOrdersActor(ActorContext<PlaceOrdersActor.Command> ctx, ExchangeClientFactory exchangeClientFactory,
                             ActorRef<PlaceOrdersResponse> updatesTo, String placeOrderActorId) {
        super(ctx);
        this.exchangeClientFactory = exchangeClientFactory;
        this.updatesTo = updatesTo;
        this.placeOrderActorId = placeOrderActorId;
        ctx.getLog().debug("Created");
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(PlaceOrders.class,this::onPlaceOrdersRequest)
                .build();
    }

    private Behavior<Command> onPlaceOrdersRequest(PlaceOrders placeOrdersRequest) {
        MarketMakerRequest marketMakerRequest = placeOrdersRequest.getMarketMakerRequest();
        BigDecimal singleAmount = placeOrdersRequest.getSingleAmount();
        BigDecimal maxPriceDelta = placeOrdersRequest.getMaxPriceDelta();
        BigDecimal minPriceDelta = placeOrdersRequest.getMinPriceDelta();

        placeOrder(marketMakerRequest, singleAmount, maxPriceDelta, minPriceDelta);

        updatesTo.tell(new PlaceOrdersResponse(placeOrderActorId));

        getContext().getLog().debug("PlaceOrdersActor {} stopped", placeOrderActorId);
        return Behaviors.stopped();
    }

    private void placeOrder(MarketMakerRequest marketMakerRequest, BigDecimal singleAmount,
                                    BigDecimal maxPriceDelta, BigDecimal minPriceDelta) {
        ExchangeClient exchangeClient = exchangeClientFactory.getExchangeClient(marketMakerRequest.getExchangeName());
        BigDecimal maxInitialPrice = marketMakerRequest.getMaxPrice();
        BigDecimal minInitialPrice = marketMakerRequest.getMinPrice();
        for (int i = 0; i < marketMakerRequest.getGridCount(); i++) {
            exchangeClient.placeOrder(marketMakerRequest.getCurrencyPair(),
                    OrderSide.SELL.toString(), OrderType.LIMIT.toString(),
                    singleAmount.toString(), maxInitialPrice.toString());
            maxInitialPrice = maxInitialPrice.subtract(maxPriceDelta, SCALE_ROUND);

            exchangeClient.placeOrder(marketMakerRequest.getCurrencyPair(),
                    OrderSide.BUY.toString(), OrderType.LIMIT.toString(),
                    singleAmount.toString(), minInitialPrice.toString());
            minInitialPrice = minInitialPrice.add(minPriceDelta, SCALE_ROUND);
        }
    }

    @Value
    public static class PlaceOrders implements Command {
        private final MarketMakerRequest marketMakerRequest;
        private final BigDecimal singleAmount;
        private final BigDecimal maxPriceDelta;
        private final BigDecimal minPriceDelta;
    }

    @Value
    public static class PlaceOrdersResponse {
        private final String placeOrderActorId;
    }

    public interface Command {
    }
}
