export interface MarketMakerShortRequest {
    exchangeName: string;
    currencyPair: string;
    needCancelOrders: boolean;
}