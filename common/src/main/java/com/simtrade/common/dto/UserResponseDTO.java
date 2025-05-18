package com.simtrade.common.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class UserResponseDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private BigDecimal balance;
    private Map<String, BigDecimal> portfolio;
    private String role;
}