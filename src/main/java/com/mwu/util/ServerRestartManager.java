package com.mwu.util;

import com.mwu.logger.Logger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ServerRestartManager {
    private static final Logger logger = new Logger();
    private Timer restartTimer;
    private Runnable onRestartCallback;
    private long restartDelayMillis = 5000;
    
    public void scheduleRestart(long delayMillis) {
        if (restartTimer != null) {
            restartTimer.cancel();
        }
        
        restartTimer = new Timer("ServerRestartTimer", true);
        long finalDelay = Math.max(delayMillis, 1000);
        
        logger.info("Server restart scheduled in " + (finalDelay / 1000) + " seconds");
        
        restartTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                performRestart();
            }
        }, finalDelay);
    }
    
    public void cancelRestart() {
        if (restartTimer != null) {
            restartTimer.cancel();
            restartTimer = null;
            logger.info("Server restart cancelled");
        }
    }
    
    private void performRestart() {
        logger.info("Initiating server restart...");
        
        try {
            if (onRestartCallback != null) {
                onRestartCallback.run();
            }
            
            String javaHome = System.getProperty("java.home");
            String classpath = System.getProperty("java.class.path");
            String mainClass = System.getProperty("sun.java.command");
            
            List<String> cmd = new ArrayList<>();
            cmd.add(javaHome + "/bin/java");
            cmd.add("-cp");
            cmd.add(classpath);
            
            if (mainClass != null && !mainClass.isEmpty()) {
                cmd.addAll(parseMainClass(mainClass));
            }
            
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.inheritIO();
            pb.start();
            
            logger.info("Server restart process started");
            System.exit(0);
            
        } catch (Exception e) {
            logger.error("Error during server restart: " + e.getMessage());
        }
    }
    
    private List<String> parseMainClass(String mainClass) {
        List<String> result = new ArrayList<>();
        String[] parts = mainClass.split(" ");
        result.add(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            result.add(parts[i]);
        }
        return result;
    }
    
    public void onRestart(Runnable callback) {
        this.onRestartCallback = callback;
    }
    
    public void setRestartDelay(long millis) {
        this.restartDelayMillis = millis;
    }
    
    public long getRestartDelay() {
        return restartDelayMillis;
    }
}
