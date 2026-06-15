package com.example.treasury.messaging;

import com.example.treasury.config.TreasuryMessagingProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class KafkaWorkflowEventPublisher implements WorkflowEventPublisher {

    private final KafkaTemplate<String, WorkflowEvent> kafkaTemplate;
    private final TreasuryMessagingProperties properties;

    public KafkaWorkflowEventPublisher(KafkaTemplate<String, WorkflowEvent> kafkaTemplate,
                                       TreasuryMessagingProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    @Override
    public void publish(WorkflowEvent event) {
        kafkaTemplate.send(properties.getKafka().getTopic(), event.getRequestId(), event);
    }
}
