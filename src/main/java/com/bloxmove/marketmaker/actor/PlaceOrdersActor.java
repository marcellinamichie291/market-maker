package com.bloxmove.marketmaker.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.bloxmove.marketmaker.model.MarketMakerRequest;
import com.bloxmove.marketmaker.model.Notification;
import com.bloxmove.marketmaker.model.NotificationStatus;
import com.bloxmove.marketmaker.model.Order;
import com.bloxmove.marketmaker.model.OrderSide;
import com.bloxmove.marketmaker.model.OrderType;
import com.bloxmove.marketmaker.service.client.ExchangeClient;
import com.bloxmove.marketmaker.service.factory.ExchangeClientFactory;
import com.bloxmove.marketmaker.service.repository.NotificationRepository;
import lombok.ToString;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.bloxmove.marketmaker.model.NotificationType.PLACE_ORDER_FAIL;
import static com.bloxmove.marketmaker.util.Constants.SCALE_ROUND;

public class PlaceOrdersActor extends AbstractBehavior<PlaceOrdersActor.Command> {

    //TODO remove in the next step
    private static final BigDecimal TARGET_PRICE = BigDecimal.valueOf(0.31);

    private final ExchangeClientFactory exchangeClientFactory;
    private final NotificationRepository notificationRepository;
    private final MarketMakerRequest marketMakerRequest;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final BigDecimal gridCount;
    private final BigDecimal singleAmount;
    private final BigDecimal maxPriceDelta;
    private final BigDecimal minPriceDelta;
    private final List<Order> orders;

    public static Behavior<PlaceOrdersActor.Command> create(ExchangeClientFactory exchangeClientFactory,
                                                            NotificationRepository notificationRepository,
                                                            MarketMakerRequest marketMakerRequest) {
        return Behaviors.logMessages(Behaviors.setup(ctx -> new PlaceOrdersActor(ctx, exchangeClientFactory,
                notificationRepository, marketMakerRequest)));
    }

    private PlaceOrdersActor(ActorContext<PlaceOrdersActor.Command> ctx, ExchangeClientFactory exchangeClientFactory,
                             NotificationRepository notificationRepository, MarketMakerRequest marketMakerRequest) {
        super(ctx);
        this.exchangeClientFactory = exchangeClientFactory;
        this.notificationRepository = notificationRepository;
        this.marketMakerRequest = marketMakerRequest;
        this.orders = new ArrayList<>();
        ctx.getLog().debug("Created");

        BigDecimal targetPrice = marketMakerRequest.getTargetPrice().multiply(TARGET_PRICE, SCALE_ROUND);
        this.minPrice = marketMakerRequest.getMinPrice().multiply(targetPrice, SCALE_ROUND);
        this.maxPrice = marketMakerRequest.getMaxPrice().multiply(targetPrice, SCALE_ROUND);
        this.gridCount = BigDecimal.valueOf(marketMakerRequest.getGridCount());
        BigDecimal maxTargetDelta = maxPrice.subtract(targetPrice, SCALE_ROUND);
        BigDecimal minTargetDelta = targetPrice.subtract(minPrice, SCALE_ROUND);
        this.singleAmount = marketMakerRequest.getAmount().divide(gridCount, SCALE_ROUND)
                .divide(BigDecimal.valueOf(2), SCALE_ROUND);
        this.maxPriceDelta = maxTargetDelta.divide(gridCount, SCALE_ROUND);
        this.minPriceDelta = minTargetDelta.divide(gridCount, SCALE_ROUND);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(PlaceOrders.class, this::onPlaceOrdersRequest)
                .onMessage(PlaceOrderResponse.class, this::onPlaceOrderResponse)
                .build();
    }

    private Behavior<Command> onPlaceOrdersRequest(PlaceOrders placeOrdersRequest) {
        placeOrders();
        return Behaviors.same();
    }

    private Behavior<Command> onPlaceOrderResponse(PlaceOrderResponse response) {
        if (response.getThrowable() != null) {
            OrderSide orderSide = response.getOrderSide();
            BigDecimal amount = response.getAmount();
            BigDecimal price = response.getPrice();
             if (response.getTriesCount() == 3) {
                 createNotification(orderSide, amount, price);
                 orders.add(new Order(0L));
            } else {
                placeOrder(orderSide, amount, price, response.getTriesCount());
            }
            return Behaviors.same();
        }

        orders.add(response.getOrder());
        if (orders.size() == gridCount.intValue() * 2) {
            getContext().getLog().debug("PlaceOrdersActor stopped");
            return Behaviors.stopped();
        }

        return Behaviors.same();
    }

    private void placeOrders() {
        BigDecimal maxInitialPrice = maxPrice;
        BigDecimal minInitialPrice = minPrice;
        for (int i = 0; i < gridCount.intValue(); i++) {
            placeOrder(OrderSide.SELL, singleAmount, maxInitialPrice, 0);
            maxInitialPrice = maxInitialPrice.subtract(maxPriceDelta, SCALE_ROUND);

            placeOrder(OrderSide.BUY, singleAmount, minInitialPrice, 0);
            minInitialPrice = minInitialPrice.add(minPriceDelta, SCALE_ROUND);
        }
    }

    private void placeOrder(OrderSide orderSide, BigDecimal amount, BigDecimal price, Integer triesCount) {
        ExchangeClient exchangeClient = exchangeClientFactory.getExchangeClient(marketMakerRequest.getExchangeName());
        CompletableFuture<Order> orderFuture = exchangeClient.placeOrder(marketMakerRequest.getCurrencyPair(),
                orderSide.toString(), OrderType.LIMIT.toString(), amount.toString(), price.toString());

        getContext().pipeToSelf(orderFuture, (order, error) ->
                new PlaceOrderResponse(orderSide, amount, price, triesCount + 1, order, error));
    }

    private void createNotification(OrderSide orderSide, BigDecimal amount, BigDecimal price) {
        Notification notification = new Notification()
                .type(PLACE_ORDER_FAIL)
                .status(NotificationStatus.OPEN)
                .message(PLACE_ORDER_FAIL.getMessage(
                        String.format("CurrencyPair = %s, side = %s, amount = %s, price = %s",
                                marketMakerRequest.getCurrencyPair(), orderSide.toString(),
                                amount.toString(), price.toString())))
                .created(Instant.now());

        notificationRepository.save(notification);
    }

    @ToString
    public enum PlaceOrders implements Command {
        INSTANCE
    }

    @Value
    public static class PlaceOrderResponse implements Command {
        private final OrderSide orderSide;
        private final BigDecimal amount;
        private final BigDecimal price;
        private final Integer triesCount;
        private final Order order;
        private final Throwable throwable;
    }

    public interface Command {
    }
}
