package com.example.treasury.domain;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "request_status_events")
public class RequestStatusEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String requestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RequestStatus status;

    @Column(length = 80)
    private String stage;

    @Column(length = 160)
    private String actor;

    @Column(length = 500)
    private String message;

    private LocalDateTime createdAt;

    protected RequestStatusEvent() {
    }

    public RequestStatusEvent(String requestId, RequestStatus status, String stage, String actor, String message) {
        this.requestId = requestId;
        this.status = status;
        this.stage = stage;
        this.actor = actor;
        this.message = message;
    }

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getRequestId() {
        return requestId;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public String getStage() {
        return stage;
    }

    public String getActor() {
        return actor;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
