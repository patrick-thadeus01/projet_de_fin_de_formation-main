package com.formation.pharmacy_manager.dto.paymentDto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.formation.pharmacy_manager.entities.PaymentMethod;
import com.formation.pharmacy_manager.entities.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponseDto {
    private long paymentId;
    private BigDecimal totalAmount;   // ✅ BigDecimal au lieu de double
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private long commandId;
    private LocalDate paymentDate;
}