package com.mwu.routing;

@FunctionalInterface
public interface RouteHandler {
    void handle(Request request, Response response) throws Exception;
}