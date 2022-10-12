package com.bloxmove.marketmaker.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.bloxmove.marketmaker.model.MarketMakerRequest;
import com.bloxmove.marketmaker.model.Notification;
import com.bloxmove.marketmaker.model.NotificationStatus;
import com.bloxmove.marketmaker.model.OrderSide;
import com.bloxmove.marketmaker.service.client.ExchangeClient;
import com.bloxmove.marketmaker.service.factory.ExchangeClientFactory;
import com.bloxmove.marketmaker.service.repository.NotificationRepository;
import lombok.ToString;
import lombok.Value;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static com.bloxmove.marketmaker.model.NotificationType.CANCEL_ORDERS_FAIL;

public class CancelOrdersActor extends AbstractBehavior<CancelOrdersActor.Command> {

    private final ExchangeClientFactory exchangeClientFactory;
    private final NotificationRepository notificationRepository;
    private final ActorRef<CancelOrdersResponse> updatesTo;
    private final MarketMakerRequest marketMakerRequest;
    private boolean canceledBuyOrders;
    private boolean canceledSellOrders;

    public static Behavior<CancelOrdersActor.Command> create(ExchangeClientFactory exchangeClientFactory,
                                                             NotificationRepository notificationRepository,
                                                             ActorRef<CancelOrdersResponse> updatesTo,
                                                             MarketMakerRequest marketMakerRequest) {
        return Behaviors.logMessages(Behaviors.setup(ctx -> new CancelOrdersActor(ctx, exchangeClientFactory,
                notificationRepository, updatesTo, marketMakerRequest)));
    }

    private CancelOrdersActor(ActorContext<CancelOrdersActor.Command> ctx, ExchangeClientFactory exchangeClientFactory,
                              NotificationRepository notificationRepository, ActorRef<CancelOrdersResponse> updatesTo,
                              MarketMakerRequest marketMakerRequest) {
        super(ctx);
        this.exchangeClientFactory = exchangeClientFactory;
        this.notificationRepository = notificationRepository;
        this.updatesTo = updatesTo;
        this.marketMakerRequest = marketMakerRequest;
        this.canceledBuyOrders = false;
        this.canceledSellOrders = false;
        ctx.getLog().debug("Created");
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(CancelOrders.class, it -> onCancelOrdersRequest())
                .onMessage(CancelOrderResponse.class, this::onCancelOrderResponse)
                .build();
    }

    private Behavior<Command> onCancelOrdersRequest() {
        cancelOrders(OrderSide.BUY, 0);
        cancelOrders(OrderSide.SELL, 0);

        return Behaviors.same();
    }

    private Behavior<Command> onCancelOrderResponse(CancelOrderResponse response) {
        int tries = response.getTries();
        OrderSide orderSide = response.getOrderSide();
        if (response.getThrowable() != null) {
            if (tries == 3) {
                setCancelOrderFlag(orderSide);
                createNotification(orderSide);
            } else {
                cancelOrders(orderSide, tries);
            }
            return Behaviors.same();
        }

        setCancelOrderFlag(orderSide);

        if (canceledSellOrders && canceledBuyOrders) {
            updatesTo.tell(CancelOrdersResponse.INSTANCE);
            canceledBuyOrders = false;
            canceledSellOrders = false;
        }

        return Behaviors.same();
    }

    private void cancelOrders(OrderSide orderSide, int tries) {
        ExchangeClient exchangeClient = exchangeClientFactory.getExchangeClient(marketMakerRequest.getExchangeName());

        CompletableFuture<Void> cancelBuyOrdersFuture =
                exchangeClient.cancelOrders(marketMakerRequest.getCurrencyPair(), orderSide.toString());
        getContext().pipeToSelf(cancelBuyOrdersFuture,
                (resp, error) -> new CancelOrderResponse(orderSide, tries + 1, error));
    }

    private void setCancelOrderFlag(OrderSide orderSide) {
        if (OrderSide.BUY.equals(orderSide)) {
            canceledBuyOrders = true;
        } else {
            canceledSellOrders = true;
        }
    }

    private void createNotification(OrderSide orderSide) {
        Notification notification = new Notification()
                .type(CANCEL_ORDERS_FAIL)
                .status(NotificationStatus.OPEN)
                .message(CANCEL_ORDERS_FAIL.getMessage(orderSide.toString()))
                .created(Instant.now());

        notificationRepository.save(notification);
    }

    @ToString
    public enum CancelOrders implements Command {
        INSTANCE
    }

    @Value
    public static class CancelOrderResponse implements Command {
        private final OrderSide orderSide;
        private final int tries;
        private final Throwable throwable;
    }

    @ToString
    public enum CancelOrdersResponse {
        INSTANCE
    }

    public interface Command {
    }
}
