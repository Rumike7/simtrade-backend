package com.simtrade.common.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.simtrade.common.enums.LoanStatus;

@Data
public class LoanResponseDTO {
    private Long id;
    private Long lenderId;
    private Long borrowerId;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer durationDays;
    private LoanStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal currentAmount;
    private LocalDateTime lastInterestApplied;

}
