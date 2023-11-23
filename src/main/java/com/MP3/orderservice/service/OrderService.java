package com.MP3.orderservice.service;

import com.MP3.orderservice.dto.InventoryRequest;
import com.MP3.orderservice.dto.InventoryResponse;
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

import java.util.Arrays;
import java.util.Iterator;
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

        // Retrieve productNames from orderRequest
        List<String> productNames = orderRequest.getOrderLineDtos()
                .stream()
                .map(OrderLineDto::getProductName)
                .toList();

        // Retrieve quantities of the orderlines/products
        List<Integer> productQuantities = orderRequest.getOrderLineDtos()
                .stream()
                .map(OrderLineDto::getQuantity)
                .toList();

        // (Synchronous communication)
        // Create InventoryRequest
        InventoryRequest inventoryRequest = new InventoryRequest();

        // Add Product Names and Quantities to the inventoryRequest
        Iterator<String> iProductNames = productNames.iterator();
        Iterator<Integer> iProductQuantities = productQuantities.iterator();
        while (iProductNames.hasNext() && iProductQuantities.hasNext()) {
            inventoryRequest.getProductAndQuantity().put(iProductNames.next(), iProductQuantities.next());
        }

        // Call the Inventory Service to check if the products are in stock
        // Inventory Service verifies if the quantity requested is in stock
        InventoryResponse[] inventoryResponseArray = webClient.get()
                .uri("http://localhost:8082/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("inventoryRequest", inventoryRequest).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        // If all requested products are in stock, then save the new order in the DB
        boolean allProductsInStock = Arrays.stream(inventoryResponseArray)
                .allMatch(InventoryResponse::isInStock);
        if (allProductsInStock) {
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
