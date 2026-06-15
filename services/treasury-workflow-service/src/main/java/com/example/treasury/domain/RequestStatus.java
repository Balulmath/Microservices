package com.example.treasury.domain;

public enum RequestStatus {
    RECEIVED,
    ELIGIBILITY_REVIEW,
    ELIGIBLE,
    PENDING_APPROVAL,
    APPROVED,
    SENT_TO_DOWNSTREAM,
    COMPLETED,
    REJECTED,
    FAILED,
    TIMED_OUT
}
