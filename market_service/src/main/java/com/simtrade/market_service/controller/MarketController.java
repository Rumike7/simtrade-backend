package com.simtrade.market_service.controller;

import com.simtrade.common.dto.StockPriceDTO;
import com.simtrade.common.dto.VolumeUpdateRequest;
import com.simtrade.market_service.entity.StockPrice;
import com.simtrade.market_service.service.MarketService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/market")
public class MarketController {
    private final MarketService service;

    public MarketController(MarketService service) {
        this.service = service;
    }

    @GetMapping("/prices/{symbol}")
    public StockPriceDTO getPrice(@PathVariable String symbol) {
        StockPrice price = service.getPrice(symbol);
        if (price == null) {
            throw new IllegalArgumentException("Unknown symbol: " + symbol);
        }
        return toDTO(price);
    }

    @PostMapping("/updateVolume")
    public ResponseEntity<StockPriceDTO> updateVolume(@RequestBody VolumeUpdateRequest request) {
        service.updateVolume(request.getSymbol(), request.getQuantity());
        StockPrice updatedPrice = service.getPrice(request.getSymbol());
        if (updatedPrice == null) {
            throw new IllegalArgumentException("Symbol not found: " + request.getSymbol());
        }
        return ResponseEntity.ok(toDTO(updatedPrice));
    }

    @GetMapping("/prices")
    public List<StockPriceDTO> getPrices(@RequestParam(required = false) List<String> symbols) {
        List<StockPrice> prices = (symbols == null || symbols.isEmpty())
                ? service.getAllPrices()
                : service.getPricesBySymbols(symbols);
        return prices.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private StockPriceDTO toDTO(StockPrice stockPrice) {
        return new StockPriceDTO(
            stockPrice.getId(),
            stockPrice.getSymbol(),
            stockPrice.getPrice(),
            stockPrice.getVolume(),
            stockPrice.getTimestamp()
        );
    }
}
