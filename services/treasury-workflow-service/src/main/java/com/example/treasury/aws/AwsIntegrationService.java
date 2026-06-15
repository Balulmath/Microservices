package com.example.treasury.aws;

import com.example.treasury.domain.TreasuryRequest;

public interface AwsIntegrationService {

    void publishOperationalTrace(TreasuryRequest request, String message);
}
