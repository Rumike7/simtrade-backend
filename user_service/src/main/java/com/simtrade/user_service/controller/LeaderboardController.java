package com.simtrade.user_service.controller;

import com.simtrade.user_service.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.simtrade.user_service.entity.LeaderboardSnapshot.LeaderboardEntry; 


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/")
    public List<LeaderboardEntry> getLeaderboard(
        @RequestParam String type,
        @RequestParam (defaultValue = "1") int page, 
        @RequestParam(defaultValue = "100") int limit) {
        return leaderboardService.getTopUsers(type, page, limit);


    }

    @GetMapping("/rank/{userId}")
    public Map<String, Object> getUserRank(
        @PathVariable Long userId,
        @RequestParam String type) {
        return leaderboardService.getUserRank(userId, type);
    }
    // TODO : history/type?
}
