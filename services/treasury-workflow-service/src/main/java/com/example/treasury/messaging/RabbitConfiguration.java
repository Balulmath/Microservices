package com.example.treasury.messaging;

import com.example.treasury.config.TreasuryMessagingProperties;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("rabbit")
public class RabbitConfiguration {

    @Bean
    public Queue treasuryWorkflowQueue(TreasuryMessagingProperties properties) {
        return new Queue(properties.getRabbit().getQueue(), true);
    }

    @Bean
    public DirectExchange treasuryWorkflowExchange(TreasuryMessagingProperties properties) {
        return new DirectExchange(properties.getRabbit().getExchange(), true, false);
    }

    @Bean
    public Binding treasuryWorkflowBinding(Queue treasuryWorkflowQueue,
                                           DirectExchange treasuryWorkflowExchange,
                                           TreasuryMessagingProperties properties) {
        return BindingBuilder
                .bind(treasuryWorkflowQueue)
                .to(treasuryWorkflowExchange)
                .with(properties.getRabbit().getRoutingKey());
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
