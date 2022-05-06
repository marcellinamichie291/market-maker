package com.bloxmove.marketmaker.service.repository;

import com.bloxmove.marketmaker.model.MarketMaker;
import com.bloxmove.marketmaker.model.MarketMakerStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketMakerRepository extends MongoRepository<MarketMaker, String> {

    MarketMaker findByCurrencyPairAndStatus(String currencyPair, MarketMakerStatus status);

    List<MarketMaker> findByStatus(MarketMakerStatus status);
}
