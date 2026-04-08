package com.utils.hardware;
import com.mwu.logger.*;

public class CPU {

    public static void log() {
        Logger logger = new Logger();


        String arch = System.getProperty("os.arch");
        int cores = Runtime.getRuntime().availableProcessors();
        String name = System.getenv("PROCESSOR_IDENTIFIER"); // Windows only (safe fallback)

        logger.info("CPU architecture: " + arch);
        logger.info("CPU cores: " + cores);

        if (name != null) {
            logger.info("CPU name: " + name);
        } else {
            logger.warn("CPU name: Unknown (OS restricted)");
        }
    }
}
