package com.example.treasury.messaging;

import com.example.treasury.service.WorkflowProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("rabbit")
public class RabbitWorkflowConsumer {

    private final WorkflowProcessor processor;

    public RabbitWorkflowConsumer(WorkflowProcessor processor) {
        this.processor = processor;
    }

    @RabbitListener(queues = "${treasury.messaging.rabbit.queue:treasury.workflow.events}")
    public void consume(WorkflowEvent event) {
        processor.process(event);
    }
}
