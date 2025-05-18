package com.simtrade.user_service.service;

import com.simtrade.common.entity.Transaction;
import com.simtrade.user_service.entity.User;
import com.simtrade.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionConsumer {

    private final UserRepository userRepository;
    private final LeaderboardService leaderboardService;

    @KafkaListener(topics = "trade-events", groupId = "leaderboard-group")
    public void handleTransaction(Transaction event) {
        List<String> userIds = Arrays.asList(event.getBuyerId(), event.getSellerId());

        for (String userIdStr : userIds) {
            Long userId = Long.parseLong(userIdStr); 
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

            BigDecimal portfolioValue = leaderboardService.calculatePortfolioValue(user);

            leaderboardService.updateUserRank(userId, user, portfolioValue, "daily");
            leaderboardService.updateUserRank(userId, user, portfolioValue, "weekly");
            leaderboardService.updateUserRank(userId, user, portfolioValue, "all-time");
        }

        System.out.println("Transaction received");
    }

}
