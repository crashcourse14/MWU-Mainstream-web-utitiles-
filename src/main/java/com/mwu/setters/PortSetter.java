package com.mwu.setters;

import com.mwu.MWU;


public class PortSetter {
    private MWU mwu;

    public PortSetter(MWU mwu) {
        this.mwu = mwu;
    }

    public int setPort(int port) {
        mwu.setPortInternal(port);
        return port;
    }
}
