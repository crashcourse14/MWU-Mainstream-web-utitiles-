package com.mwu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.utils.JSONObject;
import com.mwu.logger.Logger;

public class SettingsLoader {

    private boolean maintenance = false;
    private String host = "0.0.0.0";
    private Logger logger = new Logger();


    public SettingsLoader() {
        load();
    }

    private void load() {
        try {
            Path path = Path.of("settings.json");

            if (!Files.exists(path)) {
                logger.warn("settings.json not found. Using defaults.");
                return;
            }

            String raw = Files.readString(path);
            JSONObject json = new JSONObject(raw);

            if (json.has("maintenance")) {
                maintenance = json.getBoolean("maintenance");
            }
            if (json.has("host")) {
                host = json.getString("host");
            }

           logger.info("Loaded settings.json (maintenance = " + maintenance + ")");
           logger.info("Loaded settings.json (host = " + host + ")");

        } catch (Exception e) {
            logger.error("Could not read settings.json");
            e.printStackTrace();
        }
    }

    public boolean maintenanceEnabled() {
        return maintenance;
    }

}
