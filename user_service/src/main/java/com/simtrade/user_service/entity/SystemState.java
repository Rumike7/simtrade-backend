package com.simtrade.user_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Map;

import com.simtrade.common.enums.Trustable;

@Entity
@Table(name = "system_state")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemState implements AccountHolder {
    @Id
    final private Long id = 1L;

    @ElementCollection
    @MapKeyColumn(name = "symbol")
    @Column(name = "portfolio")
    @CollectionTable(name = "system_state_portfolio", joinColumns = @JoinColumn(name = "system_state_id"))
    private Map<String, BigDecimal> portfolio;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(nullable = false)
    private BigDecimal totalDeposits;

    @Builder.Default
    @Column(nullable = false)
    private BigDecimal interestRate = BigDecimal.valueOf(2);

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Trustable trustable = Trustable.S;

    @Column(nullable = false)
    final private String token = "TheM@tr1x";

}