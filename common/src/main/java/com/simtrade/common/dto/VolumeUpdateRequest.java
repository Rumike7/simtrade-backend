package com.simtrade.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolumeUpdateRequest {
    String symbol;
    BigDecimal quantity;
}
