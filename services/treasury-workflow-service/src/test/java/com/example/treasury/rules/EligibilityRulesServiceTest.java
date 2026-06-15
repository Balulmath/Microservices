package com.example.treasury.rules;

import com.example.treasury.domain.RequestType;
import com.example.treasury.domain.TreasuryRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EligibilityRulesServiceTest {

    private final EligibilityRulesService rulesService = new EligibilityRulesService();

    @Test
    void rejectsHighRiskRequests() {
        TreasuryRequest request = request("High Risk Client", RequestType.WIRE_SETUP, new BigDecimal("25000"), "USD", 91);

        EligibilityDecision decision = rulesService.evaluate(request);

        assertThat(decision.isEligible()).isFalse();
        assertThat(decision.getReason()).contains("Risk score");
    }

    @Test
    void requiresApprovalForLargePayments() {
        TreasuryRequest request = request("Acme Treasury", RequestType.WIRE_SETUP, new BigDecimal("150000"), "USD", 30);

        EligibilityDecision decision = rulesService.evaluate(request);

        assertThat(decision.isEligible()).isTrue();
        assertThat(decision.isApprovalRequired()).isTrue();
    }

    private TreasuryRequest request(String clientName,
                                    RequestType requestType,
                                    BigDecimal amount,
                                    String currency,
                                    Integer riskScore) {
        TreasuryRequest request = new TreasuryRequest();
        request.setClientName(clientName);
        request.setRequestType(requestType);
        request.setPaymentAmount(amount);
        request.setPaymentCurrency(currency);
        request.setRiskScore(riskScore);
        request.setCreatedBy("test");
        return request;
    }
}
