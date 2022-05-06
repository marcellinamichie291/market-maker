package com.bloxmove.marketmaker.service.mapper;

import com.bloxmove.marketmaker.model.MarketMaker;
import com.bloxmove.marketmaker.model.MarketMakerRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MarketMakerMapper {

    MarketMakerMapper INSTANCE = Mappers.getMapper(MarketMakerMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    MarketMaker map(MarketMakerRequest marketMakerRequest);

    MarketMakerRequest map(MarketMaker marketMaker);
}
