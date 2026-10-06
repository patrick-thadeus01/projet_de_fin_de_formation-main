package com.formation.pharmacy_manager.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long billId;

    // BigDecimal au lieu de double
    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;

    private LocalDate creationDate;

    @OneToOne
    @JoinColumn(name = "id_payment")
    private Payment payment;
}