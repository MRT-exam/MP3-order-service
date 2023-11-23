package com.MP3.orderservice.service;

import com.MP3.orderservice.dto.OrderLineDto;
import com.MP3.orderservice.dto.OrderRequest;
import com.MP3.orderservice.event.OrderPlacedEvent;
import com.MP3.orderservice.model.Order;
import com.MP3.orderservice.model.OrderLine;
import com.MP3.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    public void placeOrder(OrderRequest orderRequest) {
        // Create new Order model and generate an orderNumber
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        // Map orderLineDtos to orderLines and add to the order model
        List<OrderLine> orderLines = orderRequest.getOrderLineDtos()
                .stream()
                .map(this:: mapFromDto)
                .toList();
        order.setOrderLines(orderLines);

        // (Synchronous communication)
        // Call the Inventory Service to check if product is in stock
        Boolean inStock = webClient.get()
                .uri("http://localhost:8082/api/inventory")
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        // Save the new order in the DB
        if (inStock) {
            orderRepository.save(order);
            kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
        } else {
            throw new IllegalArgumentException("Product is not in stock");
        }
    }

    private OrderLine mapFromDto(OrderLineDto orderLineDto) {
        OrderLine orderLine = new OrderLine();
        orderLine.setPrice(orderLineDto.getPrice());
        orderLine.setQuantity(orderLineDto.getQuantity());
        orderLine.setProductName(orderLine.getProductName());
        return orderLine;
    }

}
