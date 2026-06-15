package com.example.treasury.rules;

import com.example.treasury.domain.TreasuryRequest;
import java.math.BigDecimal;
import org.kie.api.KieBase;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EligibilityRulesService {

    private static final Logger log = LoggerFactory.getLogger(EligibilityRulesService.class);

    private final KieBase kieBase;

    public EligibilityRulesService() {
        this.kieBase = loadRules();
    }

    public EligibilityDecision evaluate(TreasuryRequest request) {
        if (kieBase == null) {
            return evaluateInJava(request);
        }

        EligibilityFacts facts = new EligibilityFacts();
        facts.setClientName(request.getClientName());
        facts.setRequestType(request.getRequestType());
        facts.setPaymentAmount(request.getPaymentAmount());
        facts.setPaymentCurrency(request.getPaymentCurrency());
        facts.setRiskScore(request.getRiskScore());

        EligibilityDecision decision = new EligibilityDecision();
        KieSession session = kieBase.newKieSession();
        try {
            session.setGlobal("decision", decision);
            session.insert(facts);
            session.fireAllRules();
            return decision;
        } catch (RuntimeException ex) {
            log.warn("Drools evaluation failed. Falling back to Java eligibility rules: {}", ex.toString());
            return evaluateInJava(request);
        } finally {
            session.dispose();
        }
    }

    private KieBase loadRules() {
        try {
            KieHelper helper = new KieHelper();
            helper.addResource(
                    ResourceFactory.newClassPathResource("rules/treasury-eligibility.drl"),
                    ResourceType.DRL
            );
            return helper.build();
        } catch (RuntimeException ex) {
            log.warn("Drools rules could not be loaded. Java fallback rules will be used: {}", ex.toString());
            return null;
        } catch (NoClassDefFoundError error) {
            log.warn("Drools rules could not be loaded on this JDK. Java fallback rules will be used: {}", error.toString());
            return null;
        }
    }

    private EligibilityDecision evaluateInJava(TreasuryRequest request) {
        EligibilityDecision decision = new EligibilityDecision();
        if (request.getClientName() == null || request.getClientName().trim().isEmpty()) {
            decision.reject("Client name is required before treasury processing can continue");
            return decision;
        }
        String currency = request.getPaymentCurrency();
        if (currency != null && !("USD".equals(currency) || "EUR".equals(currency) || "GBP".equals(currency))) {
            decision.reject("Payment currency must be USD, EUR, or GBP for this demo workflow");
            return decision;
        }
        if (request.getRiskScore() != null && request.getRiskScore() > 85) {
            decision.reject("Risk score is above the eligibility threshold");
            return decision;
        }
        if (request.getPaymentAmount() != null
                && request.getPaymentAmount().compareTo(new BigDecimal("100000")) >= 0) {
            decision.requireApproval("Large treasury payment requires banker approval");
        }
        if (request.getRequestType() == com.example.treasury.domain.RequestType.FX_PAYMENT) {
            decision.requireApproval("FX payments require approval before downstream dispatch");
        }
        return decision;
    }
}
