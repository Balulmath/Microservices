package com.example.treasury.api;

import javax.validation.constraints.NotBlank;

public class StatusUpdateRequest {

    @NotBlank
    private String actor;

    private String reason;

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
