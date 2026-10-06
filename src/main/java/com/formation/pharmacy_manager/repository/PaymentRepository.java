package com.formation.pharmacy_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.formation.pharmacy_manager.entities.Payment;
import com.formation.pharmacy_manager.entities.PaymentMethod;
import com.formation.pharmacy_manager.entities.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Bug 1 corrigé : @Param("commandId") au lieu de @Param("command")
    @Query("SELECT p FROM Payment p WHERE p.command.commandId = :commandId ORDER BY p.paymentDate DESC")
    List<Payment> findByCommand(@Param("commandId") Long commandId);

    // Bug 2 corrigé : PaymentStatus au lieu de String
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);

    // Bug 2 corrigé : PaymentMethod au lieu de String
    List<Payment> findByPaymentMethod(PaymentMethod paymentMethod);

    Optional<Payment> findByCommand_CommandId(long commandId);
}