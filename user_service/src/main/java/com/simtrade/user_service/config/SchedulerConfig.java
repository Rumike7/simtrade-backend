package com.simtrade.user_service.config;

import com.simtrade.user_service.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class SchedulerConfig {

    private final LeaderboardService leaderboardService;

    @Scheduled(cron = "0 0 0 * * ?") // Executes daily at midnight
    public void saveDailySnapshot() {
        leaderboardService.saveDailySnapshot();
    }
}
    