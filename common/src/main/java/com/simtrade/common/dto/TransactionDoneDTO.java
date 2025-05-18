package com.simtrade.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.simtrade.common.entity.SystemState;

import java.time.Instant;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDoneDTO {
    private UserUpdateDTO userUpdateDTO;
    private SystemState systemState;
}

