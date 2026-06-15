package com.example.treasury.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "treasury.aws")
public class AwsIntegrationProperties {

    private String endpoint;
    private String region = "us-east-1";
    private String sqsQueueUrl = "http://localhost:4566/000000000000/treasury-workflow-events";
    private String snsTopicArn = "arn:aws:sns:us-east-1:000000000000:treasury-workflow-events";
    private String auditBucket = "treasury-audit";
    private String auditTable = "treasury_request_audit";

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSqsQueueUrl() {
        return sqsQueueUrl;
    }

    public void setSqsQueueUrl(String sqsQueueUrl) {
        this.sqsQueueUrl = sqsQueueUrl;
    }

    public String getSnsTopicArn() {
        return snsTopicArn;
    }

    public void setSnsTopicArn(String snsTopicArn) {
        this.snsTopicArn = snsTopicArn;
    }

    public String getAuditBucket() {
        return auditBucket;
    }

    public void setAuditBucket(String auditBucket) {
        this.auditBucket = auditBucket;
    }

    public String getAuditTable() {
        return auditTable;
    }

    public void setAuditTable(String auditTable) {
        this.auditTable = auditTable;
    }
}
