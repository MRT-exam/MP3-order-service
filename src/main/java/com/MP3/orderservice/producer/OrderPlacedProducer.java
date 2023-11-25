package com.MP3.orderservice.producer;

import com.MP3.orderservice.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderPlacedProducer {

    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public void produce(OrderPlacedEvent event) {
        kafkaTemplate.send("inventoryUpdateTopic", event);
    }
}
