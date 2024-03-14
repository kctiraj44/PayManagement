package com.example.paymanagment.service;

import com.example.paymanagment.exception.PaymentValidationException;
import com.example.paymanagment.exception.ResourceNotFoundException;
import com.example.paymanagment.model.Payment;
import com.example.paymanagment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void acceptPayment_WithValidPayment_ReturnsSavedPayment() {
        Payment payment = new Payment();
        payment.setCardNumber("1234567890123456");
        payment.setAmount(new BigDecimal("100.00"));

        given(paymentRepository.save(any(Payment.class))).willAnswer(i -> i.getArgument(0));

        Payment savedPayment = paymentService.acceptPayment(payment);

        assertNotNull(savedPayment.getTimestamp());
        assertEquals(savedPayment.getCardNumber(), payment.getCardNumber());
        assertEquals(savedPayment.getAmount(), payment.getAmount());
    }

    @Test
    public void acceptPayment_WithNegativeAmount_ThrowsException() {
        Payment payment = new Payment();
        payment.setCardNumber("1234567890123456");
        payment.setAmount(new BigDecimal("-100.00"));

        Exception exception = assertThrows(PaymentValidationException.class, () -> {
            paymentService.acceptPayment(payment);
        });

        assertEquals("Payment amount must be positive.", exception.getMessage());
    }





    @Test
    public void stopPayment_WithValidId_StopsPayment() {
        LocalDateTime now = LocalDateTime.now();
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setTimestamp(now.minusMinutes(5));

        given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

        assertTrue(paymentService.stopPayment(1L));
    }

    @Test
    public void stopPayment_After15Minutes_ThrowsException() {
        LocalDateTime now = LocalDateTime.now();
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setTimestamp(now.minusMinutes(20));

        given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            paymentService.stopPayment(1L);
        });

        assertEquals("Payment cannot be stopped after 15 minutes.", exception.getMessage());
    }







    @Test
    public void getPaymentsByCardNumber_WithExistingCard_ReturnsPayments() {
        Payment payment1 = new Payment();
        payment1.setCardNumber("1234567890123456");
        payment1.setAmount(new BigDecimal("100.00"));
        Payment payment2 = new Payment();
        payment2.setCardNumber("1234567890123456");
        payment2.setAmount(new BigDecimal("200.00"));

        given(paymentRepository.findByCardNumber("1234567890123456")).willReturn(Arrays.asList(payment1, payment2));

        List<Payment> payments = paymentService.getPaymentsByCardNumber("1234567890123456");

        assertFalse(payments.isEmpty());
        assertEquals(2, payments.size());
    }

    @Test
    public void getPaymentsByCardNumber_WithNonExistingCard_ThrowsException() {
        given(paymentRepository.findByCardNumber("non-existing")).willReturn(Arrays.asList());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            paymentService.getPaymentsByCardNumber("non-existing");
        });

        assertEquals("No payments found for card number: non-existing", exception.getMessage());
    }


}
