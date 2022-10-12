package com.bloxmove.marketmaker.controller;

import com.bloxmove.marketmaker.model.MarketMaker;
import com.bloxmove.marketmaker.model.MarketMakerRequest;
import com.bloxmove.marketmaker.model.MarketMakerShortRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "Market maker API")
@CrossOrigin
@RequestMapping(value = "/market-maker", produces = MediaType.APPLICATION_JSON_VALUE)
public interface MarketMakerController {

    @Operation(summary = "Create market maker")
    @PostMapping
    Mono<Void> create(@RequestBody @Valid @Parameter(name = "request",
            description = "Market maker request", required = true) MarketMakerRequest request);

    @Operation(summary = "Stop market maker")
    @PutMapping
    Mono<Void> stop(@RequestBody @Valid @Parameter(name = "request",
            description = "Market maker short request", required = true) MarketMakerShortRequest request);

    @Operation(summary = "Get market makers")
    @GetMapping
    List<MarketMaker> get(@Parameter(name = "status",
            description = "Market maker status", required = true) String status);
}
