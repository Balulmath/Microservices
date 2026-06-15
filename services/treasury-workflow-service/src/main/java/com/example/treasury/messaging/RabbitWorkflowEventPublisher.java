package com.example.treasury.messaging;

import com.example.treasury.config.TreasuryMessagingProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("rabbit")
public class RabbitWorkflowEventPublisher implements WorkflowEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final TreasuryMessagingProperties properties;

    public RabbitWorkflowEventPublisher(RabbitTemplate rabbitTemplate, TreasuryMessagingProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @Override
    public void publish(WorkflowEvent event) {
        rabbitTemplate.convertAndSend(
                properties.getRabbit().getExchange(),
                properties.getRabbit().getRoutingKey(),
                event
        );
    }
}
