package com.simtrade.common.client;

import com.simtrade.common.dto.BuyAssetRequestDTO;
import com.simtrade.common.dto.UserAccountDTO;
import com.simtrade.common.dto.UserResponseDTO;
import com.simtrade.common.dto.UserUpdateDTO;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UserClient {
    private final RestClient restClient;

    public UserClient(@Qualifier("userRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public UserResponseDTO getUser(String token) {
        try {
            return restClient.get()
                    .uri("/api/users/profile")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.UNAUTHORIZED, (req, res) -> {
                        throw new IllegalArgumentException("Invalid or expired token");
                    })
                    .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
                        throw new IllegalArgumentException("User not found");
                    })
                    .body(UserResponseDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to fetch user: " + e.getMessage(), e);
        }
    }

    public UserResponseDTO updateUser(String token, UserUpdateDTO userUpdateDTO) {
        try {
            return restClient.put()
                    .uri("/api/users/update")
                    .header("Authorization", "Bearer " + token)
                    .body(userUpdateDTO)
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.UNAUTHORIZED, (req, res) -> {
                        throw new IllegalArgumentException("Invalid or expired token");
                    })
                    .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
                        throw new IllegalArgumentException("User not found");
                    })
                    .body(UserResponseDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to update user: " + e.getMessage(), e);
        }
    }

    public BigDecimal updateBalance(String token, BigDecimal value) {
        try {
            return restClient.post()
                    .uri("/api/users/updateBalance/{value}" , value)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.UNAUTHORIZED, (req, res) -> {
                        throw new IllegalArgumentException("Invalid or expired token");
                    })
                    .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
                        throw new IllegalArgumentException("User not found");
                    })
                    .body(BigDecimal.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to update balance of user: " + e.getMessage(), e);
        }
    }
    public BigDecimal sendAmount(String token, Long recipientUserId, BigDecimal value) {
        try {
            return restClient.post()
                    .uri("/api/users/sendAmount/{recipientUserId}/{value}",recipientUserId, value)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.UNAUTHORIZED, (req, res) -> {
                        throw new IllegalArgumentException("Invalid or expired token");
                    })
                    .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
                        throw new IllegalArgumentException("User not found");
                    })
                    .body(BigDecimal.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to send amount of user: " + e.getMessage(), e);
        }
    }


    public String buyAsset(String token, BuyAssetRequestDTO request) {
        try {
            return restClient.post()
                    .uri("/api/users/buyAsset")
                    .header("Authorization", "Bearer " + token)
                    .body(request)
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.UNAUTHORIZED, (req, res) -> {
                        throw new IllegalArgumentException("Invalid or expired token");
                    })
                    .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
                        throw new IllegalArgumentException("User not found");
                    })
                    .body(String.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to buy asset: " + e.getMessage(), e);
        }
    }


    public UserAccountDTO getAccount(Long id) {
        try {
            return restClient.get()
                .uri("/api/users/account/{id}", id)
                .retrieve()
                    .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
                        throw new IllegalArgumentException("User not found");
                    })
                    .body(UserAccountDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to get account info of user: " + e.getMessage(), e);
        }
    }


    public BigDecimal setInterestRate(String token, BigDecimal rate) {
        try {
            return restClient.put()
                    .uri("/api/users/setInterestRate/{rate}", rate)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.UNAUTHORIZED, (req, res) -> {
                        throw new IllegalArgumentException("Invalid or expired token");
                    })
                    .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
                        throw new IllegalArgumentException("Loan not found");
                    })
                    .body(BigDecimal.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to set interest rate: " + e.getMessage(), e);
        }
    }


}