package com.formation.pharmacy_manager.services.paymentService;

import java.util.List;

import com.formation.pharmacy_manager.dto.paymentDto.PaymentRequestDto;
import com.formation.pharmacy_manager.dto.paymentDto.PaymentResponseDto;
import com.formation.pharmacy_manager.entities.PaymentMethod;
import com.formation.pharmacy_manager.entities.PaymentStatus;

public interface PaymentService {

    PaymentResponseDto createPayment(PaymentRequestDto dto);

    PaymentResponseDto getPaymentById(long paymentId);

    List<PaymentResponseDto> getPaymentsByStatus(PaymentStatus status);

    List<PaymentResponseDto> getPaymentsByMethod(PaymentMethod method);

    List<PaymentResponseDto> findByCommand(Long commandId);

    List<PaymentResponseDto> getAllPayments();

    void deletePayment(long paymentId);

    // Nouvelle méthode ajoutée
    PaymentResponseDto updatePaymentStatus(long paymentId, PaymentStatus newStatus);
}