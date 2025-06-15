package com.simtrade.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.simtrade.user_service.entity.SystemState;

@Repository
public interface SystemStateRepository extends JpaRepository<SystemState, Long> {}