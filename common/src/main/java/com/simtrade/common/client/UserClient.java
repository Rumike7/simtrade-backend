package com.simtrade.common.client;

import com.simtrade.common.dto.UserResponseDTO;
import com.simtrade.common.dto.UserUpdateDTO;
import lombok.RequiredArgsConstructor;
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
                    .uri("/api/users/account")
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
}