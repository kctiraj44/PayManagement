package com.example.paymanagment.repository;

import com.example.paymanagment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;



import java.sql.SQLException;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Retryable(value = {SQLException.class}, maxAttempts = 5, backoff = @Backoff(delay = 1000))
    Payment save(Payment payment);

    List<Payment> findByCardNumber(String cardNumber);

    List<Payment> findByCardNumberAndIsDeletedFalse(String cardNumber);

}
