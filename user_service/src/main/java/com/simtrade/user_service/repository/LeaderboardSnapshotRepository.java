package com.simtrade.user_service.repository;

import com.simtrade.user_service.entity.LeaderboardSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeaderboardSnapshotRepository extends JpaRepository<LeaderboardSnapshot, Long> {
    
    List<LeaderboardSnapshot> findByTypeOrderBySnapshotDateDesc(String type);

    List<LeaderboardSnapshot> findByTypeAndSnapshotDateBetweenOrderBySnapshotDateDesc(
        String type,
        LocalDateTime startDate,
        LocalDateTime endDate
    );
}