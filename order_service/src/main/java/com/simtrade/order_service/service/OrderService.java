package com.simtrade.order_service.service;

import com.simtrade.common.client.MarketClient;
import com.simtrade.common.client.UserClient;
import com.simtrade.common.dto.UserResponseDTO;
import com.simtrade.common.dto.UserUpdateDTO;
import com.simtrade.common.dto.BuyAssetRequestDTO;
import com.simtrade.common.dto.StockPriceDTO;
import com.simtrade.order_service.entity.Order;
import com.simtrade.order_service.entity.Order.Type;
import com.simtrade.order_service.entity.Order.PriceType;
import com.simtrade.order_service.entity.Order.Status;
import com.simtrade.common.entity.Transaction;
import com.simtrade.order_service.repository.OrderRepository;
import com.simtrade.order_service.repository.TransactionRepository;
import com.simtrade.common.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;
    private final MarketClient marketClient;
    private final UserClient userClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtUtil jwtUtil;
    private final Map<String, List<Order>> pendingOrders = new ConcurrentHashMap<>();
    private final TransactionProducer tradeEventProducer;


    @PostConstruct
    public void init() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::matchPendingOrders, 0, 1, TimeUnit.SECONDS);
    }

    @Transactional
    public Order placeOrder(String token, String symbol, Type type, PriceType priceType, BigDecimal price, BigDecimal quantity) {
        // Validate order
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        StockPriceDTO stockPrice = marketClient.getPrice(symbol);
        if (stockPrice == null) {
            throw new IllegalArgumentException("Invalid symbol: " + symbol);
        }

        // Validate token
        if (!jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        // Fetch user details
        UserResponseDTO user = userClient.getUser(token);
        Long userId = user.getId();
        String userIdStr = userId.toString();

        BigDecimal currentPrice = stockPrice.getPrice();
        Order order = Order.builder()
                .userId(userIdStr)
                .symbol(symbol)
                .type(type)
                .priceType(priceType)
                .price(price)
                .quantity(quantity)
                .status(Status.PENDING)
                .timestamp(Instant.now())
                .token(token)
                .build();

        // Validate transaction
        if (type == Type.BUY) {
            BigDecimal effectivePrice = priceType == PriceType.MARKET ? currentPrice : price;
            BigDecimal totalCost = effectivePrice.multiply(quantity);
            if (user.getBalance().compareTo(totalCost) < 0) {
                throw new IllegalStateException("Insufficient balance for buy order");
            }
        } else { // SELL
            Map<String, BigDecimal> portfolio = user.getPortfolio();
            if (portfolio.getOrDefault(symbol, BigDecimal.ZERO).compareTo(quantity) < 0) {
                throw new IllegalStateException("Insufficient stock quantity for sell order");
            }
        }

        // Save order
        order = orderRepository.save(order);

        // Execute or queue
        if (priceType == PriceType.MARKET) {
            executeMarketOrder(token, order, currentPrice);
        } else {
            queueLimitOrder(order);
        }

        // Notify user
        messagingTemplate.convertAndSend("/topic/orders/" + userIdStr, "Order placed: " + order.getType() + " " + order.getQuantity() + " " + symbol);
        return order;
    }

    @Transactional
    private void executeMarketOrder(String token, Order order, BigDecimal executionPrice) {
        String userId = order.getUserId();
        String symbol = order.getSymbol();
        BigDecimal quantity = order.getQuantity();
        BigDecimal totalCost = executionPrice.multiply(quantity);

        // Fetch user details
        UserResponseDTO user = userClient.getUser(token);
        BigDecimal currentBalance = user.getBalance();
        Map<String, BigDecimal> portfolio = user.getPortfolio();
        
        
        
        // Update balance and portfolio
        BuyAssetRequestDTO buyAssetRequestDTO = new BuyAssetRequestDTO();
        buyAssetRequestDTO.setBuying(order.getType() == Type.BUY);
        buyAssetRequestDTO.setPrice(executionPrice);
        buyAssetRequestDTO.setQuantity(quantity);
        buyAssetRequestDTO.setSymbol(symbol);
        buyAssetRequestDTO.setOtherId(-1L);
        userClient.buyAsset(token, buyAssetRequestDTO);
        
        // Update order
        order.setPrice(executionPrice);
        order.setStatus(Status.EXECUTED);
        orderRepository.save(order);

        // Log transaction
        Transaction transaction = Transaction.builder()
                .buyerId(order.getType() == Type.BUY ? userId : "Matrix")
                .sellerId(order.getType() == Type.SELL ? userId : "Matrix")
                .symbol(symbol)
                .price(executionPrice)
                .quantity(quantity)
                .timestamp(Instant.now())
                .build();
        transactionRepository.save(transaction);
        
        // Update volume
        marketClient.updateVolume(symbol, quantity);
        
        messagingTemplate.convertAndSend("/topic/transactions/" + userId, "Order executed: " + order.getType() + " " + order.getQuantity() + " " + symbol + " at $" + executionPrice);
        tradeEventProducer.sendTransaction(transaction);
        
    }

    private void queueLimitOrder(Order order) {
        pendingOrders.computeIfAbsent(order.getSymbol(), k -> new java.util.ArrayList<>()).add(order);
    }

    private void matchPendingOrders() {
        pendingOrders.forEach((symbol, orders) -> {
            StockPriceDTO stockPrice = marketClient.getPrice(symbol);
            if (stockPrice == null) return;

            BigDecimal currentPrice = stockPrice.getPrice();
            for (Order order : orders) {
                if (order.getStatus() != Status.PENDING) continue;

                // Check if the limit order can be executed
                boolean canExecute = (order.getType() == Type.BUY && order.getPrice().compareTo(currentPrice) >= 0) ||
                        (order.getType() == Type.SELL && order.getPrice().compareTo(currentPrice) <= 0);
                if (!canExecute) continue;

                // Validate token
                String token = order.getToken();
                if (!jwtUtil.validateToken(token)) {
                    order.setStatus(Status.CANCELED);
                    orderRepository.save(order);
                    messagingTemplate.convertAndSend("/topic/orders/" + order.getUserId(), "Order canceled: Token invalid or expired for order on " + symbol);
                    continue;
                }

                // Re-validate user balance/portfolio
                try {
                    UserResponseDTO user = userClient.getUser(token);
                    BigDecimal totalCost = currentPrice.multiply(order.getQuantity());
                    if (order.getType() == Type.BUY) {
                        if (user.getBalance().compareTo(totalCost) < 0) {
                            order.setStatus(Status.CANCELED);
                            orderRepository.save(order);
                            messagingTemplate.convertAndSend("/topic/orders/" + order.getUserId(), "Order canceled: Insufficient balance for buy order on " + symbol);
                            continue;
                        }
                    } else { // SELL
                        Map<String, BigDecimal> portfolio = user.getPortfolio();
                        if (portfolio.getOrDefault(symbol, BigDecimal.ZERO).compareTo(order.getQuantity()) < 0) {
                            order.setStatus(Status.CANCELED);
                            orderRepository.save(order);
                            messagingTemplate.convertAndSend("/topic/orders/" + order.getUserId(), "Order canceled: Insufficient stock quantity for sell order on " + symbol);
                            continue;
                        }
                    }

                    // Execute the limit order
                    executeMarketOrder(token, order, currentPrice);
                } catch (Exception e) {
                    order.setStatus(Status.CANCELED);
                    orderRepository.save(order);
                    messagingTemplate.convertAndSend("/topic/orders/" + order.getUserId(), "Order canceled: Failed to execute order on " + symbol + " due to " + e.getMessage());
                }
            }
            orders.removeIf(order -> order.getStatus() != Status.PENDING);
        });
    }

    public List<Order> getUserOrders(String userId, String status) {
        if (status != null && !status.isEmpty()) {
            status = status.toUpperCase();
            return orderRepository.findByUserIdAndStatus(userId, Status.valueOf(status));
        } else {
            return orderRepository.findByUserId(userId);
        }
    }

    public List<Transaction> getUserTransactions(String userId) {
        return transactionRepository.findByBuyerIdOrSellerId(userId, userId);
    }
}