package com.simtrade.market_service.service;

import com.simtrade.market_service.entity.StockPrice;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@EnableScheduling
public class MarketWebSocketService {
    private final MarketService MarketService;
    private final SimpMessagingTemplate messagingTemplate;

    public MarketWebSocketService(MarketService MarketService, SimpMessagingTemplate messagingTemplate) {
        this.MarketService = MarketService;
        this.messagingTemplate = messagingTemplate;
    }

    @Scheduled(fixedRate = 1000)
    public void broadcastPrices() {
        List<StockPrice> prices = MarketService.getAllPrices();
        messagingTemplate.convertAndSend("/topic/prices", prices);
    }
    
    @Scheduled(fixedRate = 1000)
    public void broadcastHistoricPrices() {
        List<StockPrice> prices = MarketService.getHistoricalPrices("AAPL");
        messagingTemplate.convertAndSend("/topic/historics", prices);
    }
}

