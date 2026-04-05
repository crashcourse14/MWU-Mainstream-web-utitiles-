package com.mwu.middleware;

import com.mwu.routing.Request;
import com.mwu.routing.Response;

@FunctionalInterface
public interface Middleware {
    void handle(Request request, Response response, Runnable next) throws Exception;
}