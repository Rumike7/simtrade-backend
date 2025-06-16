package com.simtrade.common.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

import com.simtrade.common.enums.Trustable;

@Data
public class UserResponseDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private BigDecimal balance;
    private Map<String, BigDecimal> portfolio;
    private String role;
    private BigDecimal totalDeposits;
    private Trustable trustable;
    private BigDecimal interestRate;
    private BigDecimal estimatedValue;
    private BigDecimal startWeekAmount;

}