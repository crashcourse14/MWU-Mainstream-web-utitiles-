package com.mwu.util;

import com.mwu.MWU;
import com.mwu.logger.Logger;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Listens for terminal commands to control the server
 * Supports: "stop" to stop the server, "restart" to restart
 */
public class CommandListener implements Runnable {
    private static final Logger logger = new Logger();
    private final MWU server;
    private volatile boolean running = true;
    
    public CommandListener(MWU server) {
        this.server = server;
    }
    
    @Override
    public void run() {
        logger.info("booting command listener...");
        
        logger.info("Command Listener Started");
        logger.info("Type: stop, restart, status, or help");
        
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(System.in));
            String command;
            
            while (running) {
                try {
                    // This will block until user enters a line
                    command = reader.readLine();
                    
                    if (command == null) {
                        continue;
                    }
                    
                    command = command.trim().toLowerCase();
                    
                    if (command.isEmpty()) {
                        continue;
                    }
                    
                    logger.info(">>> Command received: " + command);
                    
                    switch (command) {
                        case "stop":
                            handleStop();
                            return;
                        case "restart":
                            handleRestart();
                            return;
                        case "status":
                            handleStatus();
                            break;
                        case "help":
                            handleHelp();
                            break;
                        default:
                            logger.info("Unknown command: '" + command + "'. Type 'help' for available commands.");
                    }
                } catch (Exception e) {
                    if (running) {
                        logger.error("Error reading command: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Command listener error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }
    
    private void handleStop() {
        logger.info("STOPPING SERVER...");
        running = false;
        logger.info("Server stop command executed");
        server.stop();
        System.exit(0);
    }
    
    private void handleRestart() {
        logger.info("RESTARTING SERVER...");
        logger.info("Unfortunately, restart is not implemented yet. Server will be stopped instead.");
        running = false;
        logger.info("Server restart command executed");
        server.stop();
        System.exit(0);
    }
    
    private void handleStatus() {
        String status = "Server Status: Running on " + server.getHost() + ":" + server.getPort();
        logger.info(status);
    }
    
    private void handleHelp() {
        System.out.println("\n========================================");
        System.out.println("Available Commands:");
        System.out.println("  stop    - Stop the server");
        System.out.println("  restart - Restart the server");
        System.out.println("  status  - Show server status");
        System.out.println("  help    - Show this help message");
        System.out.println("========================================\n");
    }
    
    public void shutdown() {
        running = false;
    }
}
