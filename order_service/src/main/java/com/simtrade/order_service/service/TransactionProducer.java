package com.simtrade.order_service.service;

import com.simtrade.common.entity.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionProducer {

    private final KafkaTemplate<String, Transaction> kafkaTemplate;

    private static final String TOPIC = "trade-events";

    public void sendTransaction(Transaction event) {
        kafkaTemplate.send(TOPIC, event);
    }
}
