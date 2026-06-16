package com.example.treasury.api;

import com.example.treasury.domain.RequestStatus;
import com.example.treasury.domain.RequestStatusEvent;
import com.example.treasury.domain.RequestType;
import com.example.treasury.domain.TreasuryRequest;
import com.example.treasury.service.TreasuryRequestService;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/treasury/requests")
public class TreasuryRequestController {

    private final TreasuryRequestService requestService;

    public TreasuryRequestController(TreasuryRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BANKER','ADMIN')")
    public ResponseEntity<TreasuryRequestResponse> create(@Valid @RequestBody CreateTreasuryRequest input) {
        TreasuryRequest request = requestService.create(input);
        return ResponseEntity
                .created(URI.create("/api/treasury/requests/" + request.getRequestId()))
                .body(TreasuryRequestResponse.from(request));
    }

    @GetMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('BANKER','MANAGER','OPERATIONS','ADMIN')")
    public TreasuryRequestResponse get(@PathVariable String requestId) {
        return TreasuryRequestResponse.from(requestService.get(requestId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('BANKER','MANAGER','OPERATIONS','ADMIN')")
    public List<TreasuryRequestResponse> search(@RequestParam(required = false) RequestStatus status,
                                                @RequestParam(required = false) RequestType requestType,
                                                @RequestParam(required = false) String clientName) {
        List<TreasuryRequest> requests = requestService.search(status, requestType, clientName);
        List<TreasuryRequestResponse> responses = new ArrayList<TreasuryRequestResponse>();
        for (TreasuryRequest request : requests) {
            responses.add(TreasuryRequestResponse.from(request));
        }
        return responses;
    }

    @GetMapping("/{requestId}/timeline")
    @PreAuthorize("hasAnyRole('BANKER','MANAGER','OPERATIONS','ADMIN')")
    public List<RequestStatusEvent> timeline(@PathVariable String requestId) {
        return requestService.timeline(requestId);
    }

    @PostMapping("/{requestId}/approve")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public TreasuryRequestResponse approve(@PathVariable String requestId,
                                           @Valid @RequestBody StatusUpdateRequest input) {
        return TreasuryRequestResponse.from(requestService.approve(requestId, input.getActor(), input.getReason()));
    }

    @PostMapping("/{requestId}/fail")
    @PreAuthorize("hasAnyRole('OPERATIONS','ADMIN')")
    public TreasuryRequestResponse fail(@PathVariable String requestId,
                                        @Valid @RequestBody StatusUpdateRequest input) {
        return TreasuryRequestResponse.from(requestService.fail(requestId, input.getActor(), input.getReason()));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('BANKER','MANAGER','OPERATIONS','ADMIN')")
    public DashboardSummary dashboard() {
        return requestService.dashboard();
    }
}
