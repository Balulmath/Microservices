package com.example.treasury.aws;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.example.treasury.domain.TreasuryRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("aws")
public class AwsSdkIntegrationService implements AwsIntegrationService {

    private final AmazonSQS sqs;
    private final AmazonSNS sns;
    private final AmazonS3 s3;
    private final AmazonDynamoDB dynamoDB;
    private final AwsIntegrationProperties properties;

    public AwsSdkIntegrationService(AmazonSQS sqs,
                                    AmazonSNS sns,
                                    AmazonS3 s3,
                                    AmazonDynamoDB dynamoDB,
                                    AwsIntegrationProperties properties) {
        this.sqs = sqs;
        this.sns = sns;
        this.s3 = s3;
        this.dynamoDB = dynamoDB;
        this.properties = properties;
    }

    @Override
    public void publishOperationalTrace(TreasuryRequest request, String message) {
        String body = toJson(request, message);
        sqs.sendMessage(new SendMessageRequest(properties.getSqsQueueUrl(), body));
        sns.publish(new PublishRequest(properties.getSnsTopicArn(), body));
        s3.putObject(properties.getAuditBucket(), "requests/" + request.getRequestId() + ".json", body);

        Map<String, AttributeValue> item = new LinkedHashMap<String, AttributeValue>();
        item.put("request_id", new AttributeValue(request.getRequestId()));
        item.put("status", new AttributeValue(request.getStatus().name()));
        item.put("message", new AttributeValue(message));
        dynamoDB.putItem(new PutItemRequest(properties.getAuditTable(), item));
    }

    private String toJson(TreasuryRequest request, String message) {
        return "{"
                + "\"requestId\":\"" + escape(request.getRequestId()) + "\","
                + "\"status\":\"" + request.getStatus().name() + "\","
                + "\"stage\":\"" + escape(request.getCurrentStage()) + "\","
                + "\"clientName\":\"" + escape(request.getClientName()) + "\","
                + "\"message\":\"" + escape(message) + "\""
                + "}";
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
