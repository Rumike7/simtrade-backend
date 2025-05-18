package com.simtrade.user_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal totalDeposits = BigDecimal.ZERO;


    @ElementCollection
    @MapKeyColumn(name = "symbol")
    @Column(name = "quantity")
    @CollectionTable(name = "user_portfolio", joinColumns = @JoinColumn(name = "user_id"))
    private Map<String, BigDecimal> portfolio = new HashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.GUEST;

    public static enum Role {
        USER, ADMIN, MODERATOR, ANALYST, GUEST, PREMIUM_USER, TESTER
    }
}