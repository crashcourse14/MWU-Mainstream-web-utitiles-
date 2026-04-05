package com.mwu.middleware;

public interface LifecycleListener {
    default void onStart() {}
    default void onStop() {}
    
    @FunctionalInterface
    interface StartListener extends LifecycleListener {
        void onStart();
    }
    
    @FunctionalInterface
    interface StopListener extends LifecycleListener {
        void onStop();
    }
}