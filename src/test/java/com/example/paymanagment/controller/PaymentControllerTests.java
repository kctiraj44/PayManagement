package com.example.paymanagment.controller;

import com.example.paymanagment.dto.PaymentDetailDTO;
import com.example.paymanagment.dto.PaymentResponse;
import com.example.paymanagment.exception.ResourceNotFoundException;
import com.example.paymanagment.model.Payment;
import com.example.paymanagment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
public class PaymentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private Payment samplePayment;
    private PaymentResponse<Payment> samplePaymentResponse;

    @BeforeEach
    void setUp() {
        samplePayment = new Payment();
        samplePayment.setId(1L);
        samplePayment.setCardNumber("1234567890123456");
        samplePayment.setAmount(new BigDecimal("100.00"));
        samplePayment.setTimestamp(LocalDateTime.now());

        samplePaymentResponse = new PaymentResponse<>(samplePayment, "Payment processed successfully! Your transaction is now complete.");
    }

    @Test
    void acceptPayment_ShouldReturnSuccessResponse() throws Exception {
        given(paymentService.acceptPayment(any(Payment.class))).willReturn(samplePayment);

        mockMvc.perform(post("/payments/createPayemnt")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(samplePayment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(samplePaymentResponse.getMessage())));
    }

    @Test
    void stopPayment_WhenSuccessful_ShouldReturnSuccessResponse() throws Exception {
        given(paymentService.stopPayment(any(Long.class))).willReturn(true);

        mockMvc.perform(delete("/payments/delete/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Payment has been successfully stopped.")));
    }

    @Test
    void getPaymentsByCardNumber_WhenFound_ShouldReturnSuccessResponse() throws Exception {
        List<Payment> payments = Arrays.asList(samplePayment);

        given(paymentService.getPaymentsByCardNumber(any(String.class))).willReturn(payments);

        mockMvc.perform(get("/payments/card/{cardNumber}", samplePayment.getCardNumber())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Payments retrieved successfully.")))
                .andExpect(jsonPath("$.data[0].cardNumber", is(samplePayment.getCardNumber())))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void getPaymentsByCardNumber_WhenNotFound_ShouldReturnNoPaymentsFoundResponse() throws Exception {
        given(paymentService.getPaymentsByCardNumber(any(String.class))).willReturn(Arrays.asList());

        mockMvc.perform(get("/payments/card/{cardNumber}", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("No payments found for the provided card number.")))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void getActivePaymentsByCardNumber_WhenFound_ShouldReturnSuccessResponse() throws Exception {
        List<PaymentDetailDTO> paymentDetailDTOs = Arrays.asList(new PaymentDetailDTO(samplePayment.getId(), samplePayment.getCardNumber(), samplePayment.getAmount(), samplePayment.getTimestamp()));

        given(paymentService.getActivePaymentsByCardNumber(any(String.class))).willReturn(Arrays.asList(samplePayment));

        mockMvc.perform(get("/payments/active/{cardNumber}", samplePayment.getCardNumber())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Active payments retrieved successfully")))
                .andExpect(jsonPath("$.data[0].cardNumber", is(samplePayment.getCardNumber())))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void getActivePaymentsByCardNumber_WhenNotFound_ShouldReturnNotFoundResponse() throws Exception {
        given(paymentService.getActivePaymentsByCardNumber(any(String.class))).willThrow(ResourceNotFoundException.class);

        mockMvc.perform(get("/payments/active/{cardNumber}", "nonexistent"))
                .andExpect(status().isNotFound());
    }

}
