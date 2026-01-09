package com.server;

import com.mwu.MWU;
import com.mwu.logger.Logger;

public class Server {
    public static void main(String[] args) {

        Logger logger = new Logger();

        /*
        * Call the framework or else nothing will work.
        * You can also use the files MWU uses in your imports.
        * EX: import src.main.java.com.mwu.logger.Logger;
        * USE: logger.info(), logger.warn(), and logger.error().
        * A list of all imports can be found in MWUImports.txt
        */
        MWU mwu = new MWU();

        mwu.port().setPort(8080);
        mwu.dir().setPublicDirectory("public");

        try {
            mwu.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}











