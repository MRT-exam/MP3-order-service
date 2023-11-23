package com.MP3.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderLineDto {
    private int id;
    private String productName;
    private BigDecimal price;
    private int quantity;
}
