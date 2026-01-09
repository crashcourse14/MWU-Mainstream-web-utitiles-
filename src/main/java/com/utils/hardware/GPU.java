/*package com.utils.hardware;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import com.mwu.logger.*;

public class GPU {

    public static void log() {
        Logger logger = new Logger();

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();

        try {
            for (int i = 0; i < devices.length; i++) {
            GraphicsDevice d = devices[i];
            logger.info("GPU " + i + ": " + d.getIDstring());
            }
        } catch (Exception e) {
            logger.error("Can't find the GPU; Desktop enviroment needed.");
        }
    }
}*/
