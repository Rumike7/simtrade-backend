package com.simtrade.market_service.service;

import com.simtrade.common.enums.Symbol;
import com.simtrade.market_service.entity.StockPrice;
import com.simtrade.market_service.repository.StockPriceRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.function.Function;



@Service
@RequiredArgsConstructor
public class MarketService {
    private final Map<String, StockPrice> prices = new ConcurrentHashMap<>();
    private final StockPriceRepository stockPriceRepository;

    @PostConstruct
    public void init() {
        Symbol.symbols.forEach(symbol -> {
            BigDecimal randomValue = BigDecimal.valueOf(Math.random()).multiply(BigDecimal.valueOf(100));
            BigDecimal initialPrice = BigDecimal.valueOf(100).add(randomValue);
            StockPrice initialPriceEntity = new StockPrice(symbol, initialPrice, 0L);
            prices.put(symbol, initialPriceEntity);
            stockPriceRepository.save(initialPriceEntity);
        });

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::updatePrices, 0, 1, TimeUnit.SECONDS);
    }

    private void updatePrices() {
        prices.forEach((symbol, price) -> {
            BigDecimal fluctuation = BigDecimal.valueOf(Math.random() - 0.5).multiply(BigDecimal.valueOf(2));
            BigDecimal newPrice = price.getPrice().add(fluctuation);

            long newVolume = price.getVolume();

            StockPrice updatedPrice = new StockPrice(symbol, newPrice, newVolume);
            prices.put(symbol, updatedPrice);
            stockPriceRepository.save(updatedPrice);
        });

    }

    public void updateVolume(String symbol, BigDecimal quantity) {
        StockPrice currentPrice = prices.get(symbol);
        if (currentPrice == null) {
            return; 
        }

        long additionalVolume = quantity.toBigInteger().longValue(); 
        long newVolume = currentPrice.getVolume() + additionalVolume;

        StockPrice updatedPrice = new StockPrice(symbol, currentPrice.getPrice(), newVolume);
        prices.put(symbol, updatedPrice);
        stockPriceRepository.save(updatedPrice);
    }

    public StockPrice getPrice(String symbol) {
        return prices.get(symbol);
    }

    public List<StockPrice> getAllPrices() {
        return prices.values().stream().toList();
    }

    public List<StockPrice> getPricesBySymbols(List<String> symbols) {
        return symbols.stream()
                .map(prices::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<StockPrice> getHistoricalPrices(String symbol, int totalSeconds) {
        Instant now = Instant.now();
        Instant from = now.minusSeconds(totalSeconds);
        int sampleCount = 100;
        long step = totalSeconds / sampleCount;

        // Fetch all values in the range
        List<StockPrice> rawPrices = stockPriceRepository.findBySymbolAndTimestampBetween(symbol, from, now);

        // Index prices by their epoch second (rounded to nearest step)
        Map<Long, StockPrice> priceMap = rawPrices.stream()
            .collect(Collectors.toMap(
                p -> p.getTimestamp().getEpochSecond(),
                Function.identity(),
                (a, b) -> a
            ));

        // Build the result: 100 spaced values
        List<StockPrice> result = new ArrayList<>();
        for (int i = 0; i < sampleCount; i++) {
            Instant targetTime = now.minusSeconds(i * step);
            long ts = targetTime.getEpochSecond();

            StockPrice price = priceMap.getOrDefault(ts, null);
            result.add(price);
        }

        // Reverse so that it's oldest → newest (optional)
        Collections.reverse(result);
        return result;
    }
    public List<StockPrice> getAllHistoricalPrices() {
        return stockPriceRepository.findAllByOrderByTimestampDesc();
    }
}