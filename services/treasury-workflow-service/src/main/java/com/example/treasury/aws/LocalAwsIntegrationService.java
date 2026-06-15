package com.example.treasury.aws;

import com.example.treasury.domain.TreasuryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!aws")
public class LocalAwsIntegrationService implements AwsIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(LocalAwsIntegrationService.class);

    @Override
    public void publishOperationalTrace(TreasuryRequest request, String message) {
        log.info("AWS profile disabled. Request {} audit trace: {}", request.getRequestId(), message);
    }
}
