package com.formation.pharmacy_manager.dto.billDto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class BillResponseDto {

    long billId;
    long paymentId;
    LocalDate billDate;
    BigDecimal totalAmount;
    String paymentMethod;
}