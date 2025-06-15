package com.simtrade.user_service.service;

import com.simtrade.user_service.entity.User;
import com.simtrade.user_service.entity.LeaderboardSnapshot; 
import com.simtrade.user_service.entity.LeaderboardSnapshot.LeaderboardEntry; 
import com.simtrade.user_service.repository.UserRepository; 
import com.simtrade.user_service.repository.LeaderboardSnapshotRepository; 
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations; 
import org.springframework.stereotype.Service;
import com.simtrade.common.client.MarketClient;
import com.simtrade.common.dto.UserResponseDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime; 
import java.util.List; 
import java.util.Map; 
import java.util.stream.Collectors;

@Service 
@RequiredArgsConstructor 
public class LeaderboardService { 
    private static final String LEADERBOARD_KEY = "leaderboard:%s"; // e.g., leaderboard:daily 
    private final UserRepository userRepository; 
    private final LeaderboardSnapshotRepository snapshotRepository; 
    private final RedisTemplate<String, String> redisTemplate; 
    private final MarketClient marketClient;

    public BigDecimal calculatePortfolioValue(UserResponseDTO user) {
        BigDecimal stockValue = user.getPortfolio().entrySet().stream()
            .map(entry -> marketClient.getPrice(entry.getKey()).getPrice()
                .multiply(entry.getValue()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return user.getBalance().add(stockValue);
    }

    public BigDecimal calculateProfitLossPercentage(UserResponseDTO user) {
        BigDecimal portfolioValue = calculatePortfolioValue(user);
        BigDecimal totalDeposits = user.getTotalDeposits();
        if (totalDeposits.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return portfolioValue.subtract(totalDeposits)
            .divide(totalDeposits, 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
    }


    // Update user's rank in Redis
    public void updateUserRank(UserResponseDTO user, String leaderboardType) {
        Long userId = user.getId();
        BigDecimal portfolioValue = calculatePortfolioValue(user); 
        String key = String.format(LEADERBOARD_KEY, leaderboardType);
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        zSetOps.add(key, userId.toString(), portfolioValue.doubleValue());
        // Update profit/loss leaderboard
        BigDecimal profitLoss = calculateProfitLossPercentage(user);
        String profitLossKey = String.format(LEADERBOARD_KEY, leaderboardType, "profitloss");
        redisTemplate.opsForZSet().add(profitLossKey, userId.toString(), profitLoss.doubleValue());

    }

    // Get top N users from leaderboard
    String key = String.format(LEADERBOARD_KEY, "daily");
    public List<LeaderboardEntry> getTopUsers(String leaderboardType,int page, int limit) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.reverseRangeWithScores(key, 0, limit - 1).stream()
            .map(entry -> {
                User user = userRepository.findById(Long.parseLong(entry.getValue())).orElseThrow();
                LeaderboardEntry leaderboardEntry = new LeaderboardEntry();
                leaderboardEntry.setUserId(user.getId());
                leaderboardEntry.setUsername(user.getFirstName() + " " + user.getLastName());
                leaderboardEntry.setPortfolioValue(new BigDecimal(entry.getScore()));
                leaderboardEntry.setRank(zSetOps.reverseRank(key, entry.getValue()).intValue() + 1);
                return leaderboardEntry;
            })
            .collect(Collectors.toList());
    }

    // Get user's rank and nearby ranks
    public Map<String, Object> getUserRank(Long userId, String leaderboardType) {
        String key = String.format(LEADERBOARD_KEY, leaderboardType);
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        Long rank = zSetOps.reverseRank(key, userId.toString());
        if (rank == null) {
            throw new IllegalArgumentException("User not found in leaderboard");
        }

        // Fetch user and nearby ranks (e.g., 2 above and 2 below)
        long start = Math.max(0, rank - 2);
        long end = rank + 2;
        List<LeaderboardEntry> nearby = zSetOps.reverseRangeWithScores(key, start, end).stream()
            .map(entry -> {
                User user = userRepository.findById(Long.parseLong(entry.getValue())).orElseThrow();
                LeaderboardEntry leaderboardEntry = new LeaderboardEntry();
                leaderboardEntry.setUserId(user.getId());
                leaderboardEntry.setUsername(user.getFirstName() + " " + user.getLastName());
                leaderboardEntry.setPortfolioValue(new BigDecimal(entry.getScore()));
                leaderboardEntry.setRank(zSetOps.reverseRank(key, entry.getValue()).intValue() + 1);
                return leaderboardEntry;
            })
            .collect(Collectors.toList());

        return Map.of("userRank", rank + 1, "nearby", nearby);
    }

    // Save daily leaderboard snapshot
    public void saveDailySnapshot() {
        LeaderboardSnapshot snapshot = new LeaderboardSnapshot();
        snapshot.setType("daily");
        snapshot.setSnapshotDate(LocalDateTime.now());
        snapshot.setEntries(getTopUsers("daily", 1,100));
        snapshotRepository.save(snapshot);
    }



}