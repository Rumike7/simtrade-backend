package com.simtrade.common.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class UserUpdateDTO {
    private String firstName;
    private String lastName;
    private BigDecimal balance;
    private Map<String, BigDecimal> portfolio; 
}