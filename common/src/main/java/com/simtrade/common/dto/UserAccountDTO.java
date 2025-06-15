package com.simtrade.common.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

import com.simtrade.common.enums.Trustable;

@Data
public class UserAccountDTO {
    private BigDecimal balance;
    private Trustable trustable;
    private BigDecimal interestRate;
    private Map<String,BigDecimal> portfolio;
    private boolean isSystem;
}