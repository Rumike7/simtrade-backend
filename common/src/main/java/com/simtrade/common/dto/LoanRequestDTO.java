package com.simtrade.common.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class LoanRequestDTO {
    private BigDecimal amount;
    private Long lenderId;
    private Integer durationDays;
}