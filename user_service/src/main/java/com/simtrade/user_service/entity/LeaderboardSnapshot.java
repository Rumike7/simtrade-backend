package com.simtrade.user_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.*; 
import java.time.LocalDateTime; 
import java.util.List;

@Entity 
@Table(name = "leaderboard_snapshots") 
@Data 
public class LeaderboardSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // e.g., "daily", "weekly"
    
    @Column(nullable = false)
    private LocalDateTime snapshotDate;
    
    @ElementCollection
    @CollectionTable(name = "leaderboard_entries", joinColumns = @JoinColumn(name = "snapshot_id"))
    private List<LeaderboardEntry> entries;
    
    @Embeddable
    @Data
    public static class LeaderboardEntry {
        private Long userId;
        private String username;
        private BigDecimal portfolioValue;
        private Integer rank;
        private BigDecimal profitLossPercentage;
    }
        
}