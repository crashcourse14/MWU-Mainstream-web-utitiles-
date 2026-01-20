package com.utils;


import java.nio.file.Files;
import java.nio.file.Paths;

public class Settings {
    private JSONObject json;

    public Settings() throws Exception {
        String text = new String(Files.readAllBytes(Paths.get("settings.json")));
        json = new JSONObject(text);
    }

    public boolean isMaintenance() {
        return json.getBoolean("maintenance");
    }

    public boolean isTrafficMonitoringEnabled() {
        if (json.has("trafficMonitoring")) {
            return json.getBoolean("trafficMonitoring");
        }
        return true; // Default to enabled
    }

    public int getPort() {
        return json.getInt("port");
    }

    public String getHost() {
        return json.getString("host");
    }
}
