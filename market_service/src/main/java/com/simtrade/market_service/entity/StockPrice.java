package com.simtrade.market_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.Instant;
import java.math.BigDecimal;


@Entity
@Table(name = "stock_prices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockPrice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private BigDecimal total_supply;

    @Column(nullable = false)
    private long volume;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();



    public StockPrice(String symbol, BigDecimal price, long volume) {
        this.symbol = symbol;
        this.price = price;
        this.volume = volume;
        this.timestamp = Instant.now();
        this.total_supply = BigDecimal.valueOf(1000000);
    }

    

}