package com.simtrade.common.client;

import com.simtrade.common.dto.StockPriceDTO;
import com.simtrade.common.dto.VolumeUpdateRequest;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Qualifier;

@Component
public class MarketClient {
    private final RestClient restClient;

    public MarketClient(@Qualifier("marketRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public StockPriceDTO getPrice(String symbol) {
        try {
            return restClient.get()
                    .uri("/api/market/prices/{symbol}", symbol)
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
                        throw new IllegalArgumentException("Symbol not found: " + symbol);
                    })
                    .body(StockPriceDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to fetch price for symbol: " + symbol, e);
        }
    }

    public StockPriceDTO updateVolume(String symbol, BigDecimal quantity) {
        try {
            return restClient.post()
                    .uri("/api/market/updateVolume")
                    .body(new VolumeUpdateRequest(symbol, quantity))
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
                        throw new IllegalArgumentException("Symbol not found: " + symbol);
                    })
                    .onStatus(status -> status.isError(), (req, res) -> {
                        throw new RuntimeException("Failed to update volume for symbol: " + symbol + ", status: " + res.getStatusCode());
                    })
                    .body(StockPriceDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to update volume for symbol: " + symbol, e);
        }
    }

}