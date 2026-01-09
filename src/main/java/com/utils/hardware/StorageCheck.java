package com.utils.hardware;

import java.io.File;
import com.mwu.logger.*;

/**
 * Checks available disk storage.
 */
public class StorageCheck {


    private static final long TEN_GB = 10L * 1024 * 1024 * 1024;

    public static void check() {
        Logger logger = new Logger();

        File root = new File("/");

        logger.info("Checking the server space...");

        long freeBytes = root.getUsableSpace();
        long freeGB = freeBytes / (1024 * 1024 * 1024);

        if (freeBytes <= TEN_GB) {
            logger.warn("⚠ The server is low on space! (" + freeGB + " GB free)");
        } else {
            logger.info("The server has enough space (" + freeGB + " GB free)");
        }
    }
}
