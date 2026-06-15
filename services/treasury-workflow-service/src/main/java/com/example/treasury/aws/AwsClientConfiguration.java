package com.example.treasury.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("aws")
public class AwsClientConfiguration {

    @Bean
    public AmazonSQS amazonSqs(AwsIntegrationProperties properties) {
        AmazonSQSClientBuilder builder = AmazonSQSClientBuilder.standard();
        configure(builder, properties);
        return builder.build();
    }

    @Bean
    public AmazonSNS amazonSns(AwsIntegrationProperties properties) {
        AmazonSNSClientBuilder builder = AmazonSNSClientBuilder.standard();
        configure(builder, properties);
        return builder.build();
    }

    @Bean
    public AmazonS3 amazonS3(AwsIntegrationProperties properties) {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
        configure(builder, properties);
        builder.enablePathStyleAccess();
        return builder.build();
    }

    @Bean
    public AmazonDynamoDB amazonDynamoDB(AwsIntegrationProperties properties) {
        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard();
        configure(builder, properties);
        return builder.build();
    }

    private void configure(AwsClientBuilder<?, ?> builder, AwsIntegrationProperties properties) {
        builder.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("test", "test")));
        if (properties.getEndpoint() != null && !properties.getEndpoint().trim().isEmpty()) {
            builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                    properties.getEndpoint(),
                    properties.getRegion()
            ));
        } else {
            builder.withRegion(properties.getRegion());
        }
    }
}
