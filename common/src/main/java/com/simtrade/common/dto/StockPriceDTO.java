package com.simtrade.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockPriceDTO {
    private Long id;
    private String symbol;
    private BigDecimal price;
    private long volume;
    private Instant timestamp;
}
