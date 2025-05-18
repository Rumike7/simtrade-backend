package com.simtrade.order_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.math.BigDecimal;

@Entity
@Table(name = "orders")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String symbol;

    @Enumerated(EnumType.STRING)
    private Type type; // BUY or SELL

    @Enumerated(EnumType.STRING)
    private PriceType priceType; // MARKET or LIMIT

    private BigDecimal price; // Specified price for limit orders; market price for executed market orders
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    private Status status; // PENDING, EXECUTED, CANCELED

    private Instant timestamp;

    @Column(length = 5000) // JWT tokens can be long
    private String token; // Added to store JWT token

    // Constructor for initializing timestamp
    public Order(String userId, String symbol, Type type, PriceType priceType, BigDecimal price, BigDecimal quantity, String token) {
        this.userId = userId;
        this.symbol = symbol;
        this.type = type;
        this.priceType = priceType;
        this.price = price;
        this.quantity = quantity;
        this.status = Status.PENDING;
        this.timestamp = Instant.now();
        this.token = token;
    }

    public static enum Type {
        BUY, SELL
    }

    public static enum PriceType {
        MARKET, LIMIT
    }

    public static enum Status {
        PENDING, EXECUTED, CANCELED
    }
    
    @Override
    public String toString() {
        return "Order{" +
               "userId='" + userId + '\'' +
               ", symbol='" + symbol + '\'' +
               ", type=" + type +
               ", priceType=" + priceType +
               ", price=" + price +
               ", quantity=" + quantity +
               ", status=" + status +
               ", timestamp=" + timestamp +
               '}';
    }
}