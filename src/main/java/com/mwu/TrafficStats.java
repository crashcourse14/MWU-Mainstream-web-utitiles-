package com.mwu;

public class TrafficStats {
    private int totalRequests = 0;

    public void incrementRequests() {
        totalRequests++;
    }

    public int getTotalRequests() {
        return totalRequests;
    }
}