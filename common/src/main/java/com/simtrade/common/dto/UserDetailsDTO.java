package com.simtrade.common.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

import com.simtrade.common.enums.Trustable;

@Data
public class UserDetailsDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private Trustable trustable;
    private BigDecimal interestRate;
}