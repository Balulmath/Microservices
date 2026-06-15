package com.example.treasury.api;

import java.util.Map;

public class DashboardSummary {

    private long totalRequests;
    private Map<String, Long> byStatus;

    public DashboardSummary(long totalRequests, Map<String, Long> byStatus) {
        this.totalRequests = totalRequests;
        this.byStatus = byStatus;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public Map<String, Long> getByStatus() {
        return byStatus;
    }
}
