package com.example.treasury.api;

import com.example.treasury.domain.RequestStatus;
import com.example.treasury.domain.RequestType;
import com.example.treasury.domain.TreasuryRequest;
import com.example.treasury.service.TreasuryRequestService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TreasuryRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TreasuryRequestService requestService;

    @Test
    @WithMockUser(roles = "BANKER")
    void createsRequestAndAsyncProcessorCompletesIt() throws Exception {
        String response = mockMvc.perform(post("/api/treasury/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"clientName\":\"Acme Manufacturing\","
                                + "\"accountNumber\":\"782233100\","
                                + "\"requestType\":\"WIRE_SETUP\","
                                + "\"paymentAmount\":45000,"
                                + "\"paymentCurrency\":\"USD\","
                                + "\"createdBy\":\"banker\","
                                + "\"riskScore\":42"
                                + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String requestId = response.substring(response.indexOf("TR-"), response.indexOf("\",\"clientName"));
        TreasuryRequest completed = waitForCompletion(requestId);
        assertThat(completed.getStatus()).isEqualTo(RequestStatus.COMPLETED);

        mockMvc.perform(get("/api/treasury/requests/" + requestId + "/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].requestId").value(requestId));
    }

    @Test
    void managerApprovalIsRequiredForPendingRequests() throws Exception {
        String response = mockMvc.perform(post("/api/treasury/requests")
                        .with(user("banker").roles("BANKER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"clientName\":\"Summit Retail Group\","
                                + "\"accountNumber\":\"990011225\","
                                + "\"requestType\":\"FX_PAYMENT\","
                                + "\"paymentAmount\":125000,"
                                + "\"paymentCurrency\":\"USD\","
                                + "\"createdBy\":\"banker\","
                                + "\"riskScore\":51"
                                + "}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String requestId = extractRequestId(response);
        TreasuryRequest pending = waitForStatus(requestId, RequestStatus.PENDING_APPROVAL);
        assertThat(pending.getStatus()).isEqualTo(RequestStatus.PENDING_APPROVAL);

        mockMvc.perform(post("/api/treasury/requests/" + requestId + "/approve")
                        .with(user("banker").roles("BANKER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"banker\",\"reason\":\"Banker attempted approval\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/treasury/requests/" + requestId + "/approve")
                        .with(user("manager").roles("MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actor\":\"manager\",\"reason\":\"Manager approved payment\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvedBy").value("manager"));

        TreasuryRequest completed = waitForStatus(requestId, RequestStatus.COMPLETED);
        assertThat(completed.getStatus()).isEqualTo(RequestStatus.COMPLETED);
    }

    private TreasuryRequest waitForCompletion(String requestId) throws InterruptedException {
        return waitForStatus(requestId, RequestStatus.COMPLETED);
    }

    private TreasuryRequest waitForStatus(String requestId, RequestStatus status) throws InterruptedException {
        TreasuryRequest latest = requestService.get(requestId);
        for (int i = 0; i < 20; i++) {
            latest = requestService.get(requestId);
            if (latest.getStatus() == status) {
                return latest;
            }
            Thread.sleep(150L);
        }
        return latest;
    }

    private String extractRequestId(String response) {
        return response.substring(response.indexOf("TR-"), response.indexOf("\",\"clientName"));
    }
}
