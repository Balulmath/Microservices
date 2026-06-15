package com.example.treasury.service;

import com.example.treasury.api.CreateTreasuryRequest;
import com.example.treasury.api.DashboardSummary;
import com.example.treasury.domain.RequestStatus;
import com.example.treasury.domain.RequestStatusEvent;
import com.example.treasury.domain.RequestType;
import com.example.treasury.domain.TreasuryRequest;
import com.example.treasury.messaging.WorkflowEvent;
import com.example.treasury.messaging.WorkflowEventPublisher;
import com.example.treasury.messaging.WorkflowEventType;
import com.example.treasury.repository.RequestStatusEventRepository;
import com.example.treasury.repository.TreasuryRequestRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class TreasuryRequestService {

    private final TreasuryRequestRepository requestRepository;
    private final RequestStatusEventRepository eventRepository;
    private final WorkflowEventPublisher eventPublisher;

    public TreasuryRequestService(TreasuryRequestRepository requestRepository,
                                  RequestStatusEventRepository eventRepository,
                                  WorkflowEventPublisher eventPublisher) {
        this.requestRepository = requestRepository;
        this.eventRepository = eventRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TreasuryRequest create(CreateTreasuryRequest input) {
        TreasuryRequest request = new TreasuryRequest();
        request.setClientName(input.getClientName());
        request.setAccountNumber(input.getAccountNumber());
        request.setRequestType(input.getRequestType());
        request.setPaymentAmount(input.getPaymentAmount());
        request.setPaymentCurrency(input.getPaymentCurrency());
        request.setCreatedBy(input.getCreatedBy());
        request.setRiskScore(input.getRiskScore());
        request.setDestinationSystem(input.getDestinationSystem());
        request.setStatus(RequestStatus.RECEIVED);
        request.setCurrentStage("API_INTAKE");
        request.setAssignedSystem("TREASURY_WORKFLOW_SERVICE");
        request.setStatusReason("Request accepted and queued for asynchronous processing");

        TreasuryRequest saved = requestRepository.save(request);
        addHistory(saved, "API_INTAKE", input.getCreatedBy(), saved.getStatusReason());
        publishAfterCommit(WorkflowEvent.of(WorkflowEventType.REQUEST_CREATED, saved.getRequestId()));
        return saved;
    }

    @Transactional(readOnly = true)
    public TreasuryRequest get(String requestId) {
        return requestRepository.findByRequestId(requestId)
                .orElseThrow(functionNotFound(requestId));
    }

    @Transactional(readOnly = true)
    public List<TreasuryRequest> search(RequestStatus status, RequestType requestType, String clientName) {
        return requestRepository.search(status, requestType, blankToNull(clientName));
    }

    @Transactional(readOnly = true)
    public List<RequestStatusEvent> timeline(String requestId) {
        get(requestId);
        return eventRepository.findByRequestIdOrderByCreatedAtAsc(requestId);
    }

    @Transactional
    public TreasuryRequest approve(String requestId, String actor, String reason) {
        TreasuryRequest request = get(requestId);
        if (request.getStatus() != RequestStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Request " + requestId + " is not waiting for approval");
        }
        request.setApprovedBy(actor);
        request.setStatus(RequestStatus.APPROVED);
        request.setCurrentStage("APPROVAL_COMPLETE");
        request.setStatusReason(defaultText(reason, "Approved for downstream processing"));
        addHistory(request, "APPROVAL_COMPLETE", actor, request.getStatusReason());
        publishAfterCommit(WorkflowEvent.of(WorkflowEventType.APPROVAL_COMPLETED, requestId));
        return request;
    }

    @Transactional
    public TreasuryRequest fail(String requestId, String actor, String reason) {
        TreasuryRequest request = get(requestId);
        request.setStatus(RequestStatus.FAILED);
        request.setCurrentStage("MANUAL_FAILURE");
        request.setStatusReason(defaultText(reason, "Marked failed by operations"));
        addHistory(request, "MANUAL_FAILURE", actor, request.getStatusReason());
        return request;
    }

    @Transactional
    public TreasuryRequest transition(String requestId, RequestStatus status, String stage, String actor, String message) {
        TreasuryRequest request = get(requestId);
        request.setStatus(status);
        request.setCurrentStage(stage);
        request.setAssignedSystem(actor);
        request.setStatusReason(message);
        addHistory(request, stage, actor, message);
        return request;
    }

    @Transactional
    public int markTimedOutRequests(LocalDateTime cutoff) {
        List<RequestStatus> statuses = Arrays.asList(
                RequestStatus.ELIGIBILITY_REVIEW,
                RequestStatus.SENT_TO_DOWNSTREAM
        );
        List<TreasuryRequest> staleRequests = requestRepository.findTop25ByStatusInAndUpdatedAtBefore(statuses, cutoff);
        for (TreasuryRequest request : staleRequests) {
            request.setStatus(RequestStatus.TIMED_OUT);
            request.setCurrentStage("BATCH_AGING");
            request.setStatusReason("Marked timed out by Spring Batch aging job");
            addHistory(request, "BATCH_AGING", "spring-batch", request.getStatusReason());
        }
        return staleRequests.size();
    }

    @Transactional(readOnly = true)
    public DashboardSummary dashboard() {
        Map<String, Long> counts = new LinkedHashMap<String, Long>();
        for (RequestStatus status : RequestStatus.values()) {
            counts.put(status.name(), requestRepository.countByStatus(status));
        }
        return new DashboardSummary(requestRepository.count(), counts);
    }

    private void addHistory(TreasuryRequest request, String stage, String actor, String message) {
        eventRepository.save(new RequestStatusEvent(
                request.getRequestId(),
                request.getStatus(),
                stage,
                actor,
                message
        ));
    }

    private void publishAfterCommit(final WorkflowEvent event) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            eventPublisher.publish(event);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publish(event);
            }
        });
    }

    private java.util.function.Supplier<NoSuchElementException> functionNotFound(final String requestId) {
        return new java.util.function.Supplier<NoSuchElementException>() {
            @Override
            public NoSuchElementException get() {
                return new NoSuchElementException("Request " + requestId + " was not found");
            }
        };
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String defaultText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }
}
