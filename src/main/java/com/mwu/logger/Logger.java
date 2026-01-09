package com.mwu.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // All colors are in ANSI
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String ORANGE = "\u001B[38;5;214m"; // Uses 256-color mode 
    private static final String GREEN = "\u001B[32m";

    private String timestamp() {
        return LocalDateTime.now().format(formatter);
    }

    public void info(String message) {
        System.out.println("[" + timestamp() + "] [" + GREEN + "  OK  " + RESET + "] " + message); 
    }

    public void warn(String message) {
        System.out.println(ORANGE + "[" + timestamp() + "] [ WARN ] " + message + RESET);
    }

    public void error(String message) {
        System.err.println(RED + "[" + timestamp() + "] [ ERROR ] " + message + RESET);
    }

    public void log(String message) {
        System.out.println("[" + timestamp () + "]          " + message);
    }
}














