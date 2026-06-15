package com.example.treasury.messaging;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!kafka & !rabbit")
public class LocalWorkflowEventPublisher implements WorkflowEventPublisher {

    private final ApplicationEventPublisher publisher;

    public LocalWorkflowEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(WorkflowEvent event) {
        publisher.publishEvent(event);
    }
}
