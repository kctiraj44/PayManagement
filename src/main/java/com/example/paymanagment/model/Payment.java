package com.example.paymanagment.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NotNull(message = "cardNumber cannot be null")
    private String cardNumber;
    @NotNull(message = "Amount cannot be null")
    private BigDecimal amount;
    private LocalDateTime timestamp;
    @NotNull(message = "name cannot be null")
    private String name;
    private boolean isDeleted = false;

}
