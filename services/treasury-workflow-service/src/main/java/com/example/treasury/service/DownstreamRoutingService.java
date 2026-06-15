package com.example.treasury.service;

import com.example.treasury.aws.AwsIntegrationService;
import com.example.treasury.domain.RequestType;
import com.example.treasury.domain.TreasuryRequest;
import org.springframework.stereotype.Service;

@Service
public class DownstreamRoutingService {

    private final AwsIntegrationService awsIntegrationService;

    public DownstreamRoutingService(AwsIntegrationService awsIntegrationService) {
        this.awsIntegrationService = awsIntegrationService;
    }

    public String route(TreasuryRequest request) {
        String destination = request.getDestinationSystem();
        if (destination == null || destination.trim().isEmpty()) {
            destination = defaultDestination(request.getRequestType());
        }
        awsIntegrationService.publishOperationalTrace(request, "Routed to " + destination);
        return destination;
    }

    private String defaultDestination(RequestType requestType) {
        if (requestType == RequestType.WIRE_SETUP) {
            return "WIRE_PLATFORM";
        }
        if (requestType == RequestType.FX_PAYMENT) {
            return "FX_PAYMENT_GATEWAY";
        }
        if (requestType == RequestType.ACCOUNT_ACCESS_CHANGE) {
            return "ENTITLEMENT_SYSTEM";
        }
        if (requestType == RequestType.REPORTING_CHANGE) {
            return "REPORTING_SYSTEM";
        }
        return "TREASURY_SERVICE_ORCHESTRATOR";
    }
}
