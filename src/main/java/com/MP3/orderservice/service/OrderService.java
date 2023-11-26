package com.MP3.orderservice.service;

import com.MP3.orderservice.dto.InventoryResponse;
import com.MP3.orderservice.dto.OrderLineDto;
import com.MP3.orderservice.dto.OrderRequest;
import com.MP3.orderservice.event.OrderPlacedEvent;
import com.MP3.orderservice.model.Order;
import com.MP3.orderservice.model.OrderLine;
import com.MP3.orderservice.producer.OrderPlacedProducer;
import com.MP3.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;
    private final OrderPlacedProducer orderPlacedProducer;
    public void placeOrder(OrderRequest orderRequest) {
        // Create new Order model and generate an orderNumber
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        // Get stream of orderLineDtos from OrderRequest

        // Retrieve productNames
        List<String> productNames = orderRequest.getOrderLineDtos()
                .stream()
                .map(OrderLineDto::getProductName)
                .toList();
        // Retrieve quantity in each orderLine
        List<Integer> orderLineQuantityList = orderRequest.getOrderLineDtos()
                .stream()
                .map(OrderLineDto::getQuantity)
                .toList();
        // (Synchronous communication)
        // Get inventoryResponses for each orderLine
        InventoryResponse[] inventoryResponseArray = webClient.get()
                .uri("http://inventory-service:8082/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("productName", productNames).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();
        // Retrieve quantityInStock for each product
        List<Integer> inventoryQuantityInStockList = Arrays.stream(inventoryResponseArray)
                .map(InventoryResponse::getQuantityInStock)
                .toList();
        // TODO: Refactor
        // Check if all products are in stock
        boolean allProductsInStock = true;
        for (int i=0; i < inventoryQuantityInStockList.size(); i++) {
            if (inventoryQuantityInStockList.get(i) - orderLineQuantityList.get(i) >= 0) {
                continue;
            } else {
                allProductsInStock = false;
            }
        }
        // If all products are in stock then place order and send kafka events
        if (allProductsInStock) {
            // Map orderLineDtos to orderLines and add to the order model
            List<OrderLine> orderLines = orderRequest.getOrderLineDtos()
                    .stream()
                    .map(this:: mapFromDto)
                    .toList();
            order.setOrderLines(orderLines);
            orderRepository.save(order);
            // Notify the Inventory Service of the quantities ordered
            orderPlacedProducer.produce(new OrderPlacedEvent(orderRequest.getOrderLineDtos()));
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
