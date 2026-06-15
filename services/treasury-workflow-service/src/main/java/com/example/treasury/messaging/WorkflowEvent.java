package com.example.treasury.messaging;

import java.time.LocalDateTime;

public class WorkflowEvent {

    private WorkflowEventType type;
    private String requestId;
    private LocalDateTime createdAt;

    public WorkflowEvent() {
    }

    public WorkflowEvent(WorkflowEventType type, String requestId, LocalDateTime createdAt) {
        this.type = type;
        this.requestId = requestId;
        this.createdAt = createdAt;
    }

    public static WorkflowEvent of(WorkflowEventType type, String requestId) {
        return new WorkflowEvent(type, requestId, LocalDateTime.now());
    }

    public WorkflowEventType getType() {
        return type;
    }

    public void setType(WorkflowEventType type) {
        this.type = type;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
