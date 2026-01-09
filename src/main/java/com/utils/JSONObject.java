package com.utils;

import java.util.HashMap;
import java.util.Map;

public class JSONObject {

    private final Map<String, Object> data = new HashMap<>();

    public JSONObject(String json) {
        parse(json.trim());
    }

    private void parse(String json) {
        // Remove outer braces
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1).trim();
        }

        // Split key/value pairs
        String[] pairs = json.split(",");

        for (String pair : pairs) {
            String[] kv = pair.split(":", 2); // split only on first colon
            if (kv.length != 2) continue;

            String key = clean(kv[0]);
            String value = clean(kv[1]);

            // Detect boolean
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                data.put(key, Boolean.parseBoolean(value));
            }
            // Detect numbers
            else if (value.matches("-?\\d+")) {
                data.put(key, Integer.parseInt(value));
            }
            else {
                data.put(key, value);
            }
        }
    }

    private String clean(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }

    public boolean getBoolean(String key) {
        Object v = data.get(key);
        if (v instanceof Boolean) return (Boolean) v;
        throw new RuntimeException("Key '" + key + "' is not a boolean.");
    }

    public int getInt(String key) {
        Object v = data.get(key);
        if (v instanceof Number) return (Integer) v;
        throw new RuntimeException("Key '" + key + "' is not an int.");
    }

    public String getString(String key) {
        Object v = data.get(key);
        if (v instanceof String) return (String) v;
        throw new RuntimeException("Key '" + key + "' is not a string.");
    }

    public boolean has(String key) {
        return data.containsKey(key);
    }
}
