package com.mwu.setters;

import com.mwu.MWU;


public class PublicDirSetter {
    private MWU mwu;

    public PublicDirSetter(MWU mwu) {
        this.mwu = mwu;
    }

    public void setPublicDirectory(String dir) {
        mwu.setPublicDirInternal(dir);
    }
}
