package com.utils;

import com.mwu.logger.Logger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class TrafficMonitor {
    private static final Logger logger = new Logger();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Traffic metrics
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalBytesSent = new AtomicLong(0);
    private final Map<String, AtomicLong> requestsByPath = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> requestsByIP = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> requestsByMethod = new ConcurrentHashMap<>();
    private final Map<Integer, AtomicLong> requestsByStatusCode = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> requestsByUserAgent = new ConcurrentHashMap<>();

    // Performance metrics
    private final List<Long> responseTimes = new ArrayList<>();
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private volatile long minResponseTime = Long.MAX_VALUE;
    private volatile long maxResponseTime = 0;

    // Active connections tracking
    private final AtomicLong activeConnections = new AtomicLong(0);
    private volatile long peakConnections = 0;

    private final LocalDateTime startTime = LocalDateTime.now();

    public void recordRequest(String path, String clientIP, String method, String userAgent) {
        totalRequests.incrementAndGet();
        requestsByPath.computeIfAbsent(path, k -> new AtomicLong(0)).incrementAndGet();
        requestsByIP.computeIfAbsent(clientIP, k -> new AtomicLong(0)).incrementAndGet();
        requestsByMethod.computeIfAbsent(method, k -> new AtomicLong(0)).incrementAndGet();
        requestsByUserAgent.computeIfAbsent(userAgent, k -> new AtomicLong(0)).incrementAndGet();

        activeConnections.incrementAndGet();
        long currentConnections = activeConnections.get();
        if (currentConnections > peakConnections) {
            peakConnections = currentConnections;
        }

        logger.info(String.format("Request: %s %s from %s (%s active connections)",
            method, path, clientIP, currentConnections));
    }

    public void recordResponse(int statusCode, long responseTimeMs, long bytesSent) {
        requestsByStatusCode.computeIfAbsent(statusCode, k -> new AtomicLong(0)).incrementAndGet();
        totalBytesSent.addAndGet(bytesSent);
        totalResponseTime.addAndGet(responseTimeMs);

        synchronized (responseTimes) {
            responseTimes.add(responseTimeMs);
            if (responseTimes.size() > 1000) { // Keep only last 1000 response times
                responseTimes.remove(0);
            }
        }

        if (responseTimeMs < minResponseTime) minResponseTime = responseTimeMs;
        if (responseTimeMs > maxResponseTime) maxResponseTime = responseTimeMs;

        activeConnections.decrementAndGet();
    }

    public TrafficStats getStats() {
        long totalReq = totalRequests.get();
        long totalRespTime = totalResponseTime.get();
        double avgResponseTime = totalReq > 0 ? (double) totalRespTime / totalReq : 0;

        synchronized (responseTimes) {
            List<Long> timesCopy = new ArrayList<>(responseTimes);
            double medianResponseTime = calculateMedian(timesCopy);
            double p95ResponseTime = calculatePercentile(timesCopy, 95.0);

            return new TrafficStats(
                totalReq,
                totalBytesSent.get(),
                requestsByPath.entrySet().stream()
                    .sorted(Map.Entry.<String, AtomicLong>comparingByValue((a, b) -> Long.compare(b.get(), a.get())))
                    .limit(10)
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get())),
                requestsByIP.size(),
                requestsByMethod.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get())),
                requestsByStatusCode.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get())),
                avgResponseTime,
                minResponseTime == Long.MAX_VALUE ? 0 : minResponseTime,
                maxResponseTime,
                medianResponseTime,
                p95ResponseTime,
                activeConnections.get(),
                peakConnections,
                startTime
            );
        }
    }

    private double calculateMedian(List<Long> times) {
        if (times.isEmpty()) return 0;
        times.sort(Long::compare);
        int size = times.size();
        if (size % 2 == 0) {
            return (times.get(size / 2 - 1) + times.get(size / 2)) / 2.0;
        } else {
            return times.get(size / 2);
        }
    }

    private double calculatePercentile(List<Long> times, double percentile) {
        if (times.isEmpty()) return 0;
        times.sort(Long::compare);
        int index = (int) Math.ceil(percentile / 100.0 * times.size()) - 1;
        return times.get(Math.max(0, Math.min(index, times.size() - 1)));
    }

    public static class TrafficStats {
        public final long totalRequests;
        public final long totalBytesSent;
        public final Map<String, Long> topPaths;
        public final int uniqueIPs;
        public final Map<String, Long> requestsByMethod;
        public final Map<Integer, Long> requestsByStatusCode;
        public final double avgResponseTime;
        public final long minResponseTime;
        public final long maxResponseTime;
        public final double medianResponseTime;
        public final double p95ResponseTime;
        public final long activeConnections;
        public final long peakConnections;
        public final LocalDateTime startTime;

        public TrafficStats(long totalRequests, long totalBytesSent, Map<String, Long> topPaths,
                          int uniqueIPs, Map<String, Long> requestsByMethod,
                          Map<Integer, Long> requestsByStatusCode, double avgResponseTime,
                          long minResponseTime, long maxResponseTime, double medianResponseTime,
                          double p95ResponseTime, long activeConnections, long peakConnections,
                          LocalDateTime startTime) {
            this.totalRequests = totalRequests;
            this.totalBytesSent = totalBytesSent;
            this.topPaths = topPaths;
            this.uniqueIPs = uniqueIPs;
            this.requestsByMethod = requestsByMethod;
            this.requestsByStatusCode = requestsByStatusCode;
            this.avgResponseTime = avgResponseTime;
            this.minResponseTime = minResponseTime;
            this.maxResponseTime = maxResponseTime;
            this.medianResponseTime = medianResponseTime;
            this.p95ResponseTime = p95ResponseTime;
            this.activeConnections = activeConnections;
            this.peakConnections = peakConnections;
            this.startTime = startTime;
        }
    }
}