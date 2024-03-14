package com.example.paymanagment.dto;

public class PaymentResponse<T> {
    private T data;
    private String message;

    public PaymentResponse(T data, String message) {
        this.data = data;
        this.message = message;
    }

    // Getters and setters

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
