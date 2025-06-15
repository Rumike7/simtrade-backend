package com.simtrade.common.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BuyAssetRequestDTO {
    private BigDecimal price;
    private BigDecimal quantity;
    private String symbol;
    private boolean isBuying;
    private Long otherId;
}