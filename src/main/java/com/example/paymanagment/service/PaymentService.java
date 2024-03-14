package com.example.paymanagment.service;


import com.example.paymanagment.exception.PaymentValidationException;
import com.example.paymanagment.exception.ResourceNotFoundException;
import com.example.paymanagment.model.Payment;
import com.example.paymanagment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);


    @Autowired
    private PaymentRepository paymentRepository;

    @CircuitBreaker(name = "paymentService", fallbackMethod = "fallbackSavePayment")
    public Payment acceptPayment(Payment payment) {
        log.debug("Accepting payment: {}", payment.getCardNumber());
        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentValidationException("Payment amount must be positive.");
        }
        payment.setTimestamp(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public Payment fallbackSavePayment(Payment payment, Throwable t) {
        log.error("Fallback method triggered due to exception {}", t.getMessage());
        return null;
    }

    @HystrixCommand(fallbackMethod = "fallbackPayment")
    public boolean stopPayment(Long paymentId) {
        log.debug("Stopping payment with ID: {}", paymentId);
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (!paymentOpt.isPresent()) {
            throw new ResourceNotFoundException("Payment with ID " + paymentId + " not found.");
        }

        Payment payment = paymentOpt.get();
        if (payment.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            throw new PaymentValidationException("Payments over $10,000 cannot be stopped automatically. Please contact customer service.");
        }

        if (LocalDateTime.now().minusMinutes(15).isBefore(payment.getTimestamp())) {
            payment.setDeleted(true); // Mark as deleted instead of physical deletion
            paymentRepository.save(payment); // Save the updated payment
            return true;
        } else {
            throw new PaymentValidationException("Payment cannot be stopped after 15 minutes.");
        }
    }

    @HystrixCommand(fallbackMethod = "fallbackPayment")
    public List<Payment> getPaymentsByCardNumber(String cardNumber) {
        log.debug("Retrieving payments for card number: {}", cardNumber);
        List<Payment> payments = paymentRepository.findByCardNumber(cardNumber);
        if (payments.isEmpty()) {
            throw new ResourceNotFoundException("No payments found for card number: " + cardNumber);
        }
        return payments;
    }

    @HystrixCommand(fallbackMethod = "fallbackPayment")
    public List<Payment> getActivePaymentsByCardNumber(String cardNumber) {
        log.debug("Retrieving active payments for card number: {}", cardNumber);
        List<Payment> payments = paymentRepository.findByCardNumberAndIsDeletedFalse(cardNumber);

        if (payments.isEmpty()) {
            log.warn("No active payments found for card number: {}", cardNumber);
            throw new ResourceNotFoundException("No active payments found for card number: " + cardNumber);
        }
        log.info("Successfully retrieved {} active payments for card number: {}", payments.size(), cardNumber);
        return payments;
    }


}
