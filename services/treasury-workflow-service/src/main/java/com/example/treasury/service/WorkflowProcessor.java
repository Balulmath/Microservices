package com.example.treasury.service;

import com.example.treasury.domain.RequestStatus;
import com.example.treasury.domain.TreasuryRequest;
import com.example.treasury.messaging.WorkflowEvent;
import com.example.treasury.messaging.WorkflowEventType;
import com.example.treasury.rules.EligibilityDecision;
import com.example.treasury.rules.EligibilityRulesService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class WorkflowProcessor {

    private final TreasuryRequestService requestService;
    private final EligibilityRulesService eligibilityRulesService;
    private final DownstreamRoutingService downstreamRoutingService;

    public WorkflowProcessor(TreasuryRequestService requestService,
                             EligibilityRulesService eligibilityRulesService,
                             DownstreamRoutingService downstreamRoutingService) {
        this.requestService = requestService;
        this.eligibilityRulesService = eligibilityRulesService;
        this.downstreamRoutingService = downstreamRoutingService;
    }

    @Async("workflowTaskExecutor")
    public void process(WorkflowEvent event) {
        if (event.getType() == WorkflowEventType.REQUEST_CREATED || event.getType() == WorkflowEventType.RETRY_REQUESTED) {
            processNewRequest(event.getRequestId());
        } else if (event.getType() == WorkflowEventType.APPROVAL_COMPLETED) {
            dispatch(event.getRequestId());
        }
    }

    private void processNewRequest(String requestId) {
        TreasuryRequest request = requestService.transition(
                requestId,
                RequestStatus.ELIGIBILITY_REVIEW,
                "DROOLS_ELIGIBILITY",
                "eligibility-service",
                "Running eligibility and risk rules"
        );

        EligibilityDecision decision = eligibilityRulesService.evaluate(request);
        if (!decision.isEligible()) {
            requestService.transition(
                    requestId,
                    RequestStatus.REJECTED,
                    "DROOLS_ELIGIBILITY",
                    "eligibility-service",
                    decision.getReason()
            );
            return;
        }

        requestService.transition(
                requestId,
                RequestStatus.ELIGIBLE,
                "ELIGIBILITY_COMPLETE",
                "eligibility-service",
                decision.getReason()
        );

        if (decision.isApprovalRequired()) {
            requestService.transition(
                    requestId,
                    RequestStatus.PENDING_APPROVAL,
                    "APPROVAL_ROUTING",
                    "approval-service",
                    decision.getReason()
            );
            return;
        }

        dispatch(requestId);
    }

    private void dispatch(String requestId) {
        TreasuryRequest request = requestService.transition(
                requestId,
                RequestStatus.SENT_TO_DOWNSTREAM,
                "DOWNSTREAM_DISPATCH",
                "workflow-worker",
                "Publishing request to downstream treasury operation"
        );
        String destination = downstreamRoutingService.route(request);
        requestService.transition(
                requestId,
                RequestStatus.COMPLETED,
                "DOWNSTREAM_ACK",
                destination,
                "Downstream system acknowledged request"
        );
    }
}
