package com.bloxmove.marketmaker.controller;

import com.bloxmove.marketmaker.model.CurrencyBalance;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Tag(name = "Balance API")
@CrossOrigin
@RequestMapping(value = "/balance", produces = MediaType.APPLICATION_JSON_VALUE)
public interface BalanceController {

    @Operation(summary = "Get balance")
    @GetMapping
    CompletableFuture<Collection<CurrencyBalance>> get(@Parameter(name = "exchangeName",
            description = "Exchange Name", required = true) String exchangeName);
}
