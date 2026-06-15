package com.example.treasury.messaging;

import com.example.treasury.config.TreasuryMessagingProperties;
import com.example.treasury.service.WorkflowProcessor;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class KafkaWorkflowConsumer {

    private final WorkflowProcessor processor;

    public KafkaWorkflowConsumer(WorkflowProcessor processor) {
        this.processor = processor;
    }

    @KafkaListener(topics = "${treasury.messaging.kafka.topic:treasury.workflow.events}", groupId = "treasury-workflow")
    public void consume(WorkflowEvent event) {
        processor.process(event);
    }
}
