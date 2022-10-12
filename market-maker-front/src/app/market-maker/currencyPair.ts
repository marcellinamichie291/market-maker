export enum CurrencyPair {
    BLXM_USDT = 'BLXM_USDT',
    BMX_USDT = 'BMX_USDT',
    XLM_USDT = 'XLM_USDT'
}

export const CurrencyPairToIconMapping: Record<CurrencyPair, string> = {
    [CurrencyPair.BLXM_USDT]: "https://bloxmove.com/wp-content/uploads/2021/03/bloxmove_x_colour.svg",
    [CurrencyPair.BMX_USDT]: "https://s2.coinmarketcap.com/static/img/coins/64x64/2933.png",
    [CurrencyPair.XLM_USDT]: "https://s2.coinmarketcap.com/static/img/coins/64x64/512.png"
};