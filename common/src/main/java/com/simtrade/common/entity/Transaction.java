package com.simtrade.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.math.BigDecimal;


@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String buyerId;
    private String sellerId;
    private String symbol;
    private BigDecimal price;
    private BigDecimal quantity;
    private Instant timestamp;

    // Constructor for initializing timestamp
    public Transaction(String buyerId, String sellerId, String symbol, BigDecimal price, BigDecimal quantity) {
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = Instant.now();
    }
}