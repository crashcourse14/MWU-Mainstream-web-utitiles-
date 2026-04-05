package com.mwu.middleware;

import com.mwu.routing.Request;
import com.mwu.routing.Response;
import java.util.*;

/**
 * Rate limiting middleware - limits requests per IP address
 */
public class RateLimitMiddleware implements Middleware {
    private final int requestsPerMinute;
    private final Map<String, RateLimiter> limiters = new HashMap<>();
    
    public RateLimitMiddleware(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }
    
    @Override
    public void handle(Request request, Response response, Runnable next) throws Exception {
        String clientIp = request.getClientIP();
        
        RateLimiter limiter = limiters.computeIfAbsent(clientIp, 
            k -> new RateLimiter(requestsPerMinute));
        
        if (limiter.allowRequest()) {
            next.run();
        } else {
            response.status(429)
                   .header("Retry-After", "60")
                   .send("Too Many Requests");
        }
    }
    
    private static class RateLimiter {
        private final int maxRequests;
        private final Queue<Long> requestTimes = new LinkedList<>();
        private final long windowMs = 60000; // 1 minute
        
        RateLimiter(int maxRequests) {
            this.maxRequests = maxRequests;
        }
        
        synchronized boolean allowRequest() {
            long now = System.currentTimeMillis();
            
            // Remove old requests outside the window
            while (!requestTimes.isEmpty() && now - requestTimes.peek() > windowMs) {
                requestTimes.poll();
            }
            
            if (requestTimes.size() < maxRequests) {
                requestTimes.offer(now);
                return true;
            }
            
            return false;
        }
    }
}