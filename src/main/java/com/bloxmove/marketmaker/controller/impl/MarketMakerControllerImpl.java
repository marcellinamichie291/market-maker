package com.bloxmove.marketmaker.controller.impl;

import com.bloxmove.marketmaker.controller.MarketMakerController;
import com.bloxmove.marketmaker.model.MarketMaker;
import com.bloxmove.marketmaker.model.MarketMakerRequest;
import com.bloxmove.marketmaker.model.MarketMakerShortRequest;
import com.bloxmove.marketmaker.model.MarketMakerStatus;
import com.bloxmove.marketmaker.service.ExchangeManagerActorWrapper;
import com.bloxmove.marketmaker.service.repository.MarketMakerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RequestMapping(value = "/market-maker")
@RestController
@RequiredArgsConstructor
public class MarketMakerControllerImpl implements MarketMakerController {

    private final ExchangeManagerActorWrapper exchangeManagerActorWrapper;
    private final MarketMakerRepository marketMakerRepository;

    @Override
    public Mono<Void> create(MarketMakerRequest request) {
        return exchangeManagerActorWrapper.createMarketMaker(request);
    }

    @Override
    public Mono<Void> stop(MarketMakerShortRequest request) {
        return exchangeManagerActorWrapper.stopMarketMaker(request);
    }

    @Override
    public List<MarketMaker> get(String status) {
        return marketMakerRepository.findByStatus(MarketMakerStatus.valueOf(status));
    }
}
