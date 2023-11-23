package com.MP3.orderservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String orderNumber;
    @OneToMany(cascade = CascadeType.ALL)
    private List<OrderLine> orderLines;
}
