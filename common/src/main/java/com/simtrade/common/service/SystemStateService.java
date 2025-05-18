package com.simtrade.common.service;

import com.simtrade.common.entity.SystemState;
import com.simtrade.common.repository.SystemStateRepository;
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
            Map<String, BigDecimal> totalShares = new HashMap<>();
            symbols.forEach(symbol -> totalShares.put(symbol, BigDecimal.valueOf(100)));

            SystemState systemState = SystemState.builder()
                    .totalShares(totalShares)
                    .totalDollarValue(BigDecimal.valueOf(100000))
                    .build();

            systemStateRepository.save(systemState);
        }
    }

    public SystemState getSystemState() {
        return systemStateRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("System state not initialized"));
    }

    public void updateSystemState(Map<String, BigDecimal> totalShares, BigDecimal totalDollarValue) {
        SystemState systemState = getSystemState();
        systemState.setTotalShares(totalShares);
        systemState.setTotalDollarValue(totalDollarValue);
        systemStateRepository.save(systemState);
    }
}