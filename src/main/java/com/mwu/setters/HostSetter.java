package com.mwu.setters;

import com.mwu.MWU;


public class HostSetter {
    private MWU mwu;

    public HostSetter(MWU mwu) {
        this.mwu = mwu;
    }

    public String setHost(String host) {
        mwu.setHostInternal(host);
        return host;
    }
}
