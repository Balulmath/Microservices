package com.example.treasury.rules;

public class EligibilityDecision {

    private boolean eligible = true;
    private String reason = "Eligible for workflow processing";
    private boolean approvalRequired;

    public boolean isEligible() {
        return eligible;
    }

    public String getReason() {
        return reason;
    }

    public boolean isApprovalRequired() {
        return approvalRequired;
    }

    public void reject(String reason) {
        this.eligible = false;
        this.reason = reason;
    }

    public void requireApproval(String reason) {
        this.approvalRequired = true;
        this.reason = reason;
    }
}
