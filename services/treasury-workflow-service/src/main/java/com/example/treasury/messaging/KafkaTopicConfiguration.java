package com.example.treasury.messaging;

import com.example.treasury.config.TreasuryMessagingProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("kafka")
public class KafkaTopicConfiguration {

    @Bean
    public NewTopic treasuryWorkflowTopic(TreasuryMessagingProperties properties) {
        return new NewTopic(properties.getKafka().getTopic(), 3, (short) 1);
    }
}
