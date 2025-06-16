package com.simtrade.order_service.repository;

import com.simtrade.order_service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdAndStatus(String userId, Order.Status status);
    List<Order> findByUserId(String userId);
    List<Order> findByStatus(Order.Status status);
    List<Order> findBySymbolAndStatus(String symbol, Order.Status status);

}