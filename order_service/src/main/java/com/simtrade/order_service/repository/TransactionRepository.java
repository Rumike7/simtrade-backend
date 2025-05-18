package com.simtrade.order_service.repository;

import com.simtrade.common.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByBuyerIdOrSellerId(String buyerId, String sellerId);
}