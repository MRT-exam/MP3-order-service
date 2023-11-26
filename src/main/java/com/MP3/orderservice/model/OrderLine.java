package com.MP3.orderservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Table(name = "order_lines")
@Entity
public class OrderLine {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String productName;
    private BigDecimal price;
    private int quantity;
}