export interface MarketMakerRequest {
    exchangeName: string;
    currencyPair: string;
    amount: number;
    minPrice: number;
    targetPrice: number;
    maxPrice: number;
    delay: number;
    gridCount: number;
    minFirstCurrency: number;
    minSecondCurrency: number;
    needCancelOrders: boolean;
}