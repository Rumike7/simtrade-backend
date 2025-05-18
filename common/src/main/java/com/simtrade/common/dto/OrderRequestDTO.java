package com.simtrade.common.dto;

import lombok.Data;
import java.math.BigDecimal;


@Data
public class OrderRequestDTO {
    private String symbol;
    private String type;       // "BUY" or "SELL"
    private String priceType;  // "MARKET" or "LIMIT"
    private BigDecimal price;
    private BigDecimal quantity;

}