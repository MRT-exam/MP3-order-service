package com.MP3.orderservice.service;

import com.MP3.orderservice.dto.OrderLineDto;
import com.MP3.orderservice.dto.OrderRequest;
import com.MP3.orderservice.model.Order;
import com.MP3.orderservice.model.OrderLine;
import com.MP3.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLine> orderLines = orderRequest.getOrderLineDtos()
                .stream()
                .map(this:: mapFromDto)
                .toList();
        order.setOrderLines(orderLines);
        orderRepository.save(order);
    }

    private OrderLine mapFromDto(OrderLineDto orderLineDto) {
        OrderLine orderLine = new OrderLine();
        orderLine.setPrice(orderLineDto.getPrice());
        orderLine.setQuantity(orderLineDto.getQuantity());
        orderLine.setProductName(orderLine.getProductName());
        return orderLine;
    }

}
