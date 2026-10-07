package com.example.carrental.dto;

public class PaymentResultDto {

    private boolean success;
    private String paymentId;
    private String transactionId;
    private String message;
    private String receiptUrl;

    public PaymentResultDto() {
    }

    public PaymentResultDto(boolean success, String paymentId, String transactionId, String message, String receiptUrl) {
        this.success = success;
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.message = message;
        this.receiptUrl = receiptUrl;
    }

    public static PaymentResultDto success(String paymentId, String transactionId, String receiptUrl) {
        return new PaymentResultDto(true, paymentId, transactionId, "Payment processed successfully.", receiptUrl);
    }

    public static PaymentResultDto failure(String message) {
        return new PaymentResultDto(false, null, null, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiptUrl() {
        return receiptUrl;
    }

    public void setReceiptUrl(String receiptUrl) {
        this.receiptUrl = receiptUrl;
    }
}
