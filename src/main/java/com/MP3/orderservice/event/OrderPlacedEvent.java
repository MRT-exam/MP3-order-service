package com.MP3.orderservice.event;

import com.MP3.orderservice.dto.OrderLineDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPlacedEvent {
    private List<OrderLineDto> orderLineDtos;
}
