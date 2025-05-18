package com.simtrade.order_service.controller;

import com.simtrade.order_service.entity.Order;
import com.simtrade.order_service.entity.Order.Type;
import com.simtrade.order_service.entity.Order.PriceType;
import com.simtrade.common.entity.Transaction;
import com.simtrade.order_service.service.OrderService;
import com.simtrade.common.util.JwtUtil;
import com.simtrade.common.dto.OrderRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    @PostMapping("/place")
    @PreAuthorize("hasAnyRole('USER', 'PREMIUM_USER')")
    public ResponseEntity<Order> placeOrder(
        @RequestHeader("Authorization") String authorizationHeader,
        @RequestBody OrderRequestDTO orderRequest) {

            String token = authorizationHeader.replace("Bearer ", "");
            Order order = orderService.placeOrder(
                    token,
                    orderRequest.getSymbol(),
                    Type.valueOf(orderRequest.getType()),
                    PriceType.valueOf(orderRequest.getPriceType()),
                    orderRequest.getPrice(),
                    orderRequest.getQuantity());

            return ResponseEntity.ok(order);
        }


    @GetMapping(value = {"/history", "/history/{status}"})
    @PreAuthorize("hasAnyRole('USER', 'PREMIUM_USER')")
    public ResponseEntity<List<Order>> getUserOrders(
        @RequestHeader("Authorization") String authorizationHeader,
        @PathVariable(required = false) String status) {
        String token = authorizationHeader.replace("Bearer ", "");
        String userId = String.valueOf(jwtUtil.getUserIdFromToken(token));
        return ResponseEntity.ok(orderService.getUserOrders(userId, status));
    }


    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('USER', 'PREMIUM_USER')")
    public ResponseEntity<List<Transaction>> getUserTransactions(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        String userId = String.valueOf(jwtUtil.getUserIdFromToken(token));
        return ResponseEntity.ok(orderService.getUserTransactions(userId));
    }


      
}