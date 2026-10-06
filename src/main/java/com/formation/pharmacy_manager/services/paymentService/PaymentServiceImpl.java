package com.formation.pharmacy_manager.services.paymentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.formation.pharmacy_manager.dto.paymentDto.PaymentRequestDto;
import com.formation.pharmacy_manager.dto.paymentDto.PaymentResponseDto;
import com.formation.pharmacy_manager.entities.Bill;
import com.formation.pharmacy_manager.entities.Command;
import com.formation.pharmacy_manager.entities.Payment;
import com.formation.pharmacy_manager.entities.PaymentMethod;
import com.formation.pharmacy_manager.entities.PaymentStatus;
import com.formation.pharmacy_manager.exceptions.ForbiddenException;
import com.formation.pharmacy_manager.repository.BillRepository;
import com.formation.pharmacy_manager.repository.CommandRepository;
import com.formation.pharmacy_manager.repository.PaymentRepository;
import com.formation.pharmacy_manager.security.SecurityUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final CommandRepository commandRepository;
    private final BillRepository billRepository;
    private final SecurityUtils securityUtils;

    @Override
    public PaymentResponseDto createPayment(PaymentRequestDto dto) {
        Command command = commandRepository.findById(dto.getCommandId())
                .orElseThrow(() -> new RuntimeException("Commande introuvable : " + dto.getCommandId()));

        // ✅ Sécurité : un non-admin ne peut payer que sa propre commande
        securityUtils.checkOwnershipOrAdmin(command.getUser().getUserName());

        BigDecimal totalAmount = calculateCommandTotal(command);

        Payment payment = new Payment();
        payment.setCommand(command);
        payment.setTotalAmount(totalAmount);
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDate.now());

        Payment saved = paymentRepository.save(payment);
        return toDto(saved);
    }

    @Override
    public PaymentResponseDto getPaymentById(long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Paiement introuvable : " + paymentId));

        // ✅ Sécurité
        securityUtils.checkOwnershipOrAdmin(payment.getCommand().getUser().getUserName());

        return toDto(payment);
    }

    @Override
    public List<PaymentResponseDto> getPaymentsByStatus(PaymentStatus status) {
        if (!securityUtils.isCurrentUserAdmin()) {
            throw new ForbiddenException("Accès réservé aux administrateurs.");
        }
        return paymentRepository.findByPaymentStatus(status).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponseDto> getPaymentsByMethod(PaymentMethod method) {
        if (!securityUtils.isCurrentUserAdmin()) {
            throw new ForbiddenException("Accès réservé aux administrateurs.");
        }
        return paymentRepository.findByPaymentMethod(method).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponseDto> findByCommand(Long commandId) {
        // ✅ Sécurité : vérifier que la commande appartient à l'utilisateur
        Command command = commandRepository.findById(commandId)
                .orElseThrow(() -> new RuntimeException("Commande introuvable : " + commandId));
        securityUtils.checkOwnershipOrAdmin(command.getUser().getUserName());

        return paymentRepository.findByCommand(commandId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponseDto> getAllPayments() {
        // ✅ Sécurité : admin = tout, user = ses paiements uniquement
        if (securityUtils.isCurrentUserAdmin()) {
            return paymentRepository.findAll().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }
        String currentUserName = securityUtils.getCurrentUserName();
        return paymentRepository.findAll().stream()
                .filter(p -> p.getCommand() != null
                        && p.getCommand().getUser() != null
                        && currentUserName.equals(p.getCommand().getUser().getUserName()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePayment(long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Paiement introuvable : " + paymentId));

        // ✅ Sécurité
        securityUtils.checkOwnershipOrAdmin(payment.getCommand().getUser().getUserName());

        paymentRepository.deleteById(paymentId);
    }

    @Override
    public PaymentResponseDto updatePaymentStatus(long paymentId, PaymentStatus newStatus) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Paiement introuvable : " + paymentId));

        // ✅ Sécurité
        securityUtils.checkOwnershipOrAdmin(payment.getCommand().getUser().getUserName());

        payment.setPaymentStatus(newStatus);

        // Si le paiement est finalisé, on génère la facture (si pas déjà fait)
        if (newStatus == PaymentStatus.FINISHED && payment.getBill() == null) {
            Bill bill = new Bill();
            bill.setPayment(payment);
            bill.setTotalAmount(payment.getTotalAmount());
            bill.setCreationDate(LocalDate.now());
            billRepository.save(bill);
            payment.setBill(bill);
        }

        Payment updated = paymentRepository.save(payment);
        return toDto(updated);
    }

    // ===========================
    // Méthodes utilitaires
    // ===========================

    private BigDecimal calculateCommandTotal(Command command) {
        if (command.getCommandDrugList() == null || command.getCommandDrugList().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return command.getCommandDrugList().stream()
                .map(cd -> BigDecimal.valueOf(cd.getDrug().getPrice())
                        .multiply(BigDecimal.valueOf(cd.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PaymentResponseDto toDto(Payment payment) {
        return new PaymentResponseDto(
                payment.getPaymentId(),
                payment.getTotalAmount(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getCommand().getCommandId(),
                payment.getPaymentDate()
        );
    }
}