package com.simtrade.user_service.entity;

import java.math.BigDecimal;
import java.util.Map;

import com.simtrade.common.enums.Trustable;

public interface AccountHolder {
    Long getId();
    BigDecimal getBalance();
    void setBalance(BigDecimal balance);
    Map<String, BigDecimal> getPortfolio();
    void setPortfolio(Map<String, BigDecimal> portfolio);
    BigDecimal getInterestRate();
    void setInterestRate(BigDecimal interestRate);
    Trustable getTrustable();
    void setTrustable(Trustable trustable);   
}
