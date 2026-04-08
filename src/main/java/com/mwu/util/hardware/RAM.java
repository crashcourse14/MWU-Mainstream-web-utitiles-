package com.utils.hardware;

import com.mwu.logger.*;

public class RAM {

    public static void log() {
        Logger logger = new Logger();

        Runtime rt = Runtime.getRuntime();

        long total = rt.totalMemory();
        long free = rt.freeMemory();
        long used = total - free;
        long max = rt.maxMemory();

        try {
            logger.info("Memory used: " + toMB(used) + " MB");
            logger.info("Memory free: " + toMB(free) + " MB");
            logger.info("Total memory allocated: " + toMB(total) + " MB");
            logger.info("Max memory allowed: " + toMB(max) + " MB");
        } catch (Exception e) {
            logger.error("Cant load RAM; Dekstop enviroment not found.");
        }
    }

    private static long toMB(long bytes) {
        return bytes / (1024 * 1024);
    }
}
