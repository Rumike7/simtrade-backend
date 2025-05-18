package com.simtrade.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Map;

@Entity
@Table(name = "system_state")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @MapKeyColumn(name = "symbol")
    @Column(name = "shares")
    @CollectionTable(name = "system_state_shares", joinColumns = @JoinColumn(name = "system_state_id"))
    private Map<String, BigDecimal> totalShares; // Maps symbol to total shares (e.g., AAPL -> 100)

    @Column(name = "total_dollar_value", nullable = false)
    private BigDecimal totalDollarValue; // Total system dollar value (e.g., $100,000)
}