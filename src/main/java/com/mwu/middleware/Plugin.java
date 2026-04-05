package com.mwu.middleware;

import com.mwu.MWU;

public interface Plugin {
    void initialize(MWU mwu);
    
    default String getName() {
        return this.getClass().getSimpleName();
    }
    
    default String getVersion() {
        return "1.0.0";
    }
}