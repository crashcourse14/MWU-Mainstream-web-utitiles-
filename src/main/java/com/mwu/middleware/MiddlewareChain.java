package com.mwu.middleware;

import com.mwu.routing.Request;
import com.mwu.routing.Response;
import java.util.*;

public class MiddlewareChain {
    private final List<Middleware> middlewares = new ArrayList<>();
    
    public void add(Middleware middleware) {
        middlewares.add(middleware);
    }
    
    public void execute(Request request, Response response, Runnable finalHandler) throws Exception {
        new Chain(middlewares, finalHandler).proceed(request, response);
    }
    
    public int size() {
        return middlewares.size();
    }
    
    private static class Chain {
        private final List<Middleware> middlewares;
        private final Runnable finalHandler;
        private int index = 0;
        
        Chain(List<Middleware> middlewares, Runnable finalHandler) {
            this.middlewares = middlewares;
            this.finalHandler = finalHandler;
        }
        
        void proceed(Request request, Response response) throws Exception {
            if (index < middlewares.size()) {
                Middleware middleware = middlewares.get(index++);
                middleware.handle(request, response, () -> {
                    try {
                        proceed(request, response);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } else {
                finalHandler.run();
            }
        }
    }
}