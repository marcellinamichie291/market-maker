import { CurrencyPair } from "./currencyPair";
import { ExchangeName } from "./exchangeName";
import { MarketMakerStatus } from "./marketMakerStatus";

export interface MarketMaker {
    id: string;
    exchangeName: ExchangeName;
    currencyPair: CurrencyPair;
    amount: number;
    minPrice: number;
    targetPrice: number;
    maxPrice: number;
    delay: number;
    gridCount: number;
    minFirstCurrency: number;
    minSecondCurrency: number;
    status: MarketMakerStatus;
    created: string;
    updated: string;
}