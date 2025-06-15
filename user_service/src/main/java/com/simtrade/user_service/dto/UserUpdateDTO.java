package com.simtrade.user_service.dto;

import java.math.BigDecimal;
import java.util.Map;

import com.simtrade.common.enums.Trustable;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String firstName;
    private String lastName;
    private String password;
    private BigDecimal balance;
    private Map<String, BigDecimal> portfolio;
    private Trustable trustable;
}