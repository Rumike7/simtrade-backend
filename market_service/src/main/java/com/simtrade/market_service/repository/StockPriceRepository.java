package com.simtrade.market_service.repository;

import com.simtrade.market_service.entity.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {
    List<StockPrice> findBySymbolAndTimestampAfter(String symbol, Instant timestamp);
    List<StockPrice> findAllByOrderByTimestampDesc();
    List<StockPrice> findBySymbolAndTimestampBetween(String symbol, Instant from, Instant now);
}