package com.mwu.routing;

public interface WebSocketHandler {
    void onConnect(WebSocketConnection connection);
    void onMessage(WebSocketConnection connection, String message);
    void onClose(WebSocketConnection connection);
    void onError(WebSocketConnection connection, Throwable error);
}