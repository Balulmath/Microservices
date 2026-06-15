package com.example.treasury;

import com.example.treasury.aws.AwsIntegrationProperties;
import com.example.treasury.config.TreasuryMessagingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties({TreasuryMessagingProperties.class, AwsIntegrationProperties.class})
public class TreasuryWorkflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(TreasuryWorkflowApplication.class, args);
    }

    @Bean(name = "workflowTaskExecutor")
    public AsyncTaskExecutor workflowTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(12);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("treasury-workflow-");
        executor.initialize();
        return executor;
    }
}
