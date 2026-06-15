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

    private TreasuryRequest waitForCompletion(String requestId) throws InterruptedException {
        TreasuryRequest latest = requestService.get(requestId);
        for (int i = 0; i < 20; i++) {
            latest = requestService.get(requestId);
            if (latest.getStatus() == RequestStatus.COMPLETED) {
                return latest;
            }
            Thread.sleep(150L);
        }
        return latest;
    }
}
