package com.simtrade.market_service.service;

import com.simtrade.market_service.entity.StockPrice;
import com.simtrade.common.entity.SystemState;
import com.simtrade.common.service.SystemStateService;
import com.simtrade.market_service.repository.StockPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MarketService {
    private final Map<String, StockPrice> prices = new ConcurrentHashMap<>();
    private final StockPriceRepository stockPriceRepository;
    private final SystemStateService systemStateService;
    private final List<String> symbols = Arrays.asList("AAPL", "GOOGL", "MSFT");

    @PostConstruct
    public void init() {
        // Initialize stock prices with data from system state
        SystemState systemState = systemStateService.getSystemState();
        symbols.forEach(symbol -> {
            // Random initial price between $100-$200
            BigDecimal randomValue = BigDecimal.valueOf(Math.random()).multiply(BigDecimal.valueOf(100));
            BigDecimal initialPrice = BigDecimal.valueOf(100).add(randomValue);
            StockPrice initialPriceEntity = new StockPrice(symbol, initialPrice, 0L); // Initial volume is 0
            prices.put(symbol, initialPriceEntity);
            stockPriceRepository.save(initialPriceEntity);
        });

        // Start price and volume update scheduler
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::updatePrices, 0, 1, TimeUnit.SECONDS);
    }

    private void updatePrices() {
        SystemState systemState = systemStateService.getSystemState();
        prices.forEach((symbol, price) -> {
            // Update price with small random fluctuation (±1)
            BigDecimal fluctuation = BigDecimal.valueOf(Math.random() - 0.5).multiply(BigDecimal.valueOf(2));
            BigDecimal newPrice = price.getPrice().add(fluctuation);

            long newVolume = price.getVolume();

            StockPrice updatedPrice = new StockPrice(symbol, newPrice, newVolume);
            prices.put(symbol, updatedPrice);
            stockPriceRepository.save(updatedPrice);
        });

        // Verify total shares remain at 100 per symbol
        Map<String, BigDecimal> totalShares = systemState.getTotalShares();
        symbols.forEach(symbol -> {
            if (totalShares.getOrDefault(symbol, BigDecimal.ZERO).compareTo(BigDecimal.valueOf(100)) != 0) {
                totalShares.put(symbol, BigDecimal.valueOf(100)); // Enforce 100 shares per symbol
            }
        });
        systemStateService.updateSystemState(totalShares, systemState.getTotalDollarValue());
    }

    public void updateVolume(String symbol, BigDecimal quantity) {
        StockPrice currentPrice = prices.get(symbol);
        if (currentPrice == null) {
            return; // Symbol not found, silently ignore (or throw exception if preferred)
        }

        // Increment volume by the transacted quantity (convert BigDecimal to long)
        long additionalVolume = quantity.toBigInteger().longValue(); // Assumes whole shares
        long newVolume = currentPrice.getVolume() + additionalVolume;

        // Create updated StockPrice with new volume
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

    public List<StockPrice> getHistoricalPrices(String symbol) {
        return stockPriceRepository.findBySymbolAndTimestampAfter(symbol, Instant.now().minusSeconds(30 * 24 * 60 * 60)); // Last 30 days
    }

    public List<StockPrice> getAllHistoricalPrices() {
        return stockPriceRepository.findAllByOrderByTimestampDesc();
    }
}