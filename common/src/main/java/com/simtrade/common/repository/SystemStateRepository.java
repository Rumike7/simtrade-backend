package com.simtrade.common.repository;

import com.simtrade.common.entity.SystemState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemStateRepository extends JpaRepository<SystemState, Long> {}