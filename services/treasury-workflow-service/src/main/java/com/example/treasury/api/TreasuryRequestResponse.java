package com.example.treasury.api;

import com.example.treasury.domain.RequestStatus;
import com.example.treasury.domain.RequestType;
import com.example.treasury.domain.TreasuryRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TreasuryRequestResponse {

    private String requestId;
    private String clientName;
    private String accountNumber;
    private RequestType requestType;
    private RequestStatus status;
    private String currentStage;
    private String assignedSystem;
    private String destinationSystem;
    private BigDecimal paymentAmount;
    private String paymentCurrency;
    private Integer riskScore;
    private String createdBy;
    private String approvedBy;
    private String statusReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TreasuryRequestResponse from(TreasuryRequest request) {
        TreasuryRequestResponse response = new TreasuryRequestResponse();
        response.requestId = request.getRequestId();
        response.clientName = request.getClientName();
        response.accountNumber = request.getAccountNumber();
        response.requestType = request.getRequestType();
        response.status = request.getStatus();
        response.currentStage = request.getCurrentStage();
        response.assignedSystem = request.getAssignedSystem();
        response.destinationSystem = request.getDestinationSystem();
        response.paymentAmount = request.getPaymentAmount();
        response.paymentCurrency = request.getPaymentCurrency();
        response.riskScore = request.getRiskScore();
        response.createdBy = request.getCreatedBy();
        response.approvedBy = request.getApprovedBy();
        response.statusReason = request.getStatusReason();
        response.createdAt = request.getCreatedAt();
        response.updatedAt = request.getUpdatedAt();
        return response;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getClientName() {
        return clientName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public String getAssignedSystem() {
        return assignedSystem;
    }

    public String getDestinationSystem() {
        return destinationSystem;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public String getPaymentCurrency() {
        return paymentCurrency;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
