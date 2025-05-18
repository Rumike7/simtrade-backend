package com.simtrade.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;


@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Add an interceptor to include the Authorization header with a token (if needed)
        ClientHttpRequestInterceptor authInterceptor = (request, body, execution) -> {
            // Example: Add Authorization header if needed
            // request.getHeaders().add("Authorization", "Bearer " + token);
            return execution.execute(request, body);
        };
        // restTemplate.setInterceptors(Collections.singletonList(authInterceptor));

        return restTemplate;
    }

    @Bean(name = "marketRestClient")
    public RestClient marketRestClient(@Value("${market.service.url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean(name = "userRestClient")
    public RestClient userRestClient(@Value("${user.service.url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}