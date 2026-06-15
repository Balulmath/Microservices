package com.example.treasury.batch;

import com.example.treasury.service.TreasuryRequestService;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class RequestAgingBatchConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RequestAgingBatchConfiguration.class);

    @Bean
    public Job requestAgingJob(JobBuilderFactory jobs, Step requestAgingStep) {
        return jobs.get("requestAgingJob")
                .start(requestAgingStep)
                .build();
    }

    @Bean
    public Step requestAgingStep(StepBuilderFactory steps, final TreasuryRequestService requestService) {
        return steps.get("requestAgingStep")
                .tasklet((contribution, chunkContext) -> {
                    int updated = requestService.markTimedOutRequests(LocalDateTime.now().minusMinutes(30));
                    log.info("Spring Batch aging job marked {} requests as timed out", updated);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
