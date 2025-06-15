package com.simtrade.user_service.service;

import com.simtrade.common.enums.Trustable;
import com.simtrade.user_service.entity.SystemState;
import com.simtrade.user_service.repository.SystemStateRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemStateService {
    private final SystemStateRepository systemStateRepository;
    private final List<String> symbols = List.of("AAPL", "GOOGL", "MSFT");

    @PostConstruct
    public void initializeSystemState() {
        // Check if system state already exists
        if (systemStateRepository.count() == 0) {
         // Initialize with 100 shares per symbol and $100,000 total dollar value
            Map<String, BigDecimal> portfolio = new HashMap<>();
            symbols.forEach(symbol -> portfolio.put(symbol, BigDecimal.valueOf(1000000)));

            SystemState systemState = SystemState.builder()
                .portfolio(portfolio)
                .balance(BigDecimal.valueOf(1000000000))
                .totalDeposits(BigDecimal.valueOf(1000000000))
                .build();


            systemStateRepository.save(systemState);
        }
    }

    public SystemState getSystemState() {
        return systemStateRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("System state not initialized"));
    }

    public void updateSystemState(Map<String, BigDecimal> portfolio, BigDecimal balance) {
        SystemState systemState = getSystemState();
        systemState.setPortfolio(portfolio);
        systemState.setBalance(balance);
        // systemState.setTotalDeposits(balance);
        systemStateRepository.save(systemState);
    }
}