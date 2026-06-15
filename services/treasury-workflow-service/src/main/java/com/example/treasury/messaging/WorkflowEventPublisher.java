package com.example.treasury.messaging;

public interface WorkflowEventPublisher {

    void publish(WorkflowEvent event);
}
