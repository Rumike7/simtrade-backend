package com.simtrade.common.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class RepaymentRequestDTO {
    private BigDecimal amount;
}