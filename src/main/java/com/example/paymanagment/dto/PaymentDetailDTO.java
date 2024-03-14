package com.example.paymanagment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaymentDetailDTO {
    private Long id;
    private String cardNumber;
    private BigDecimal amount;
    private LocalDateTime timestamp;



}
