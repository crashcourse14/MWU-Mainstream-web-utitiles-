package com.mwu.util;

import java.util.*;

/**
 * Enhanced JSONObject with improved parsing and fluent API
 * Supports nested objects, arrays, and better type handling
 */
public class JSONObject {
    private final Map<String, Object> data = new LinkedHashMap<>();
    
    public JSONObject() {}
    
    public JSONObject(String json) {
        if (json != null && !json.trim().isEmpty()) {
            parse(json.trim());
        }
    }
    
    public JSONObject(Map<String, Object> map) {
        this.data.putAll(map);
    }
    
    private void parse(String json) {
        json = json.trim();
        
        if (!json.startsWith("{") || !json.endsWith("}")) {
            throw new IllegalArgumentException("JSON must start with { and end with }");
        }
        
        json = json.substring(1, json.length() - 1).trim();
        
        if (json.isEmpty()) {
            return;
        }
        
        parseObject(json, data);
    }
    
    private void parseObject(String json, Map<String, Object> target) {
        int i = 0;
        int length = json.length();
        
        while (i < length) {
            // Skip whitespace
            while (i < length && Character.isWhitespace(json.charAt(i))) {
                i++;
            }
            
            if (i >= length) break;
            
            // Parse key
            if (json.charAt(i) != '"') {
                throw new IllegalArgumentException("Expected \" at position " + i);
            }
            
            int keyStart = i + 1;
            int keyEnd = findClosingQuote(json, keyStart);
            String key = json.substring(keyStart, keyEnd);
            i = keyEnd + 1;
            
            // Skip whitespace and colon
            while (i < length && (Character.isWhitespace(json.charAt(i)) || json.charAt(i) == ':')) {
                i++;
            }
            
            // Parse value
            ParseResult result = parseValue(json, i);
            target.put(key, result.value);
            i = result.endIndex;
            
            // Skip whitespace and comma
            while (i < length && (Character.isWhitespace(json.charAt(i)) || json.charAt(i) == ',')) {
                i++;
            }
        }
    }
    
    private ParseResult parseValue(String json, int start) {
        char c = json.charAt(start);
        
        // String
        if (c == '"') {
            int end = findClosingQuote(json, start + 1);
            return new ParseResult(json.substring(start + 1, end), end + 1);
        }
        
        // Number
        if (c == '-' || Character.isDigit(c)) {
            int end = start;
            boolean isDecimal = false;
            
            while (end < json.length()) {
                char ch = json.charAt(end);
                if (ch == '.' || ch == 'e' || ch == 'E') {
                    isDecimal = true;
                    end++;
                } else if (Character.isDigit(ch) || ch == '-' || ch == '+') {
                    end++;
                } else {
                    break;
                }
            }
            
            String numStr = json.substring(start, end);
            try {
                if (isDecimal) {
                    return new ParseResult(Double.parseDouble(numStr), end);
                } else {
                    long num = Long.parseLong(numStr);
                    if (num >= Integer.MIN_VALUE && num <= Integer.MAX_VALUE) {
                        return new ParseResult((int) num, end);
                    }
                    return new ParseResult(num, end);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number: " + numStr);
            }
        }
        
        // Boolean
        if (json.startsWith("true", start)) {
            return new ParseResult(true, start + 4);
        }
        if (json.startsWith("false", start)) {
            return new ParseResult(false, start + 5);
        }
        
        // Null
        if (json.startsWith("null", start)) {
            return new ParseResult(null, start + 4);
        }
        
        // Object
        if (c == '{') {
            int end = findClosingBrace(json, start);
            String objStr = json.substring(start + 1, end);
            Map<String, Object> nestedObj = new LinkedHashMap<>();
            parseObject(objStr, nestedObj);
            return new ParseResult(new JSONObject(nestedObj), end + 1);
        }
        
        // Array
        if (c == '[') {
            int end = findClosingBracket(json, start);
            String arrStr = json.substring(start + 1, end);
            List<Object> array = parseArray(arrStr);
            return new ParseResult(array, end + 1);
        }
        
        throw new IllegalArgumentException("Unexpected character at position " + start + ": " + c);
    }
    
    private List<Object> parseArray(String json) {
        List<Object> array = new ArrayList<>();
        
        if (json.trim().isEmpty()) {
            return array;
        }
        
        int i = 0;
        while (i < json.length()) {
            // Skip whitespace
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
                i++;
            }
            
            if (i >= json.length()) break;
            
            ParseResult result = parseValue(json, i);
            array.add(result.value);
            i = result.endIndex;
            
            // Skip whitespace and comma
            while (i < json.length() && (Character.isWhitespace(json.charAt(i)) || json.charAt(i) == ',')) {
                i++;
            }
        }
        
        return array;
    }
    
    private int findClosingQuote(String json, int start) {
        for (int i = start; i < json.length(); i++) {
            if (json.charAt(i) == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unclosed string starting at position " + start);
    }
    
    private int findClosingBrace(String json, int start) {
        return findClosing(json, start, '{', '}');
    }
    
    private int findClosingBracket(String json, int start) {
        return findClosing(json, start, '[', ']');
    }
    
    private int findClosing(String json, int start, char open, char close) {
        int depth = 0;
        boolean inString = false;
        
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inString = !inString;
            } else if (!inString) {
                if (c == open) {
                    depth++;
                } else if (c == close) {
                    depth--;
                    if (depth == 0) {
                        return i;
                    }
                }
            }
        }
        
        throw new IllegalArgumentException("Unclosed " + open + " starting at position " + start);
    }
    
    // ==================== Getters ====================
    
    public Object get(String key) {
        return data.get(key);
    }
    
    public String getString(String key) {
        Object value = data.get(key);
        if (value == null) {
            throw new RuntimeException("Key '" + key + "' not found");
        }
        if (value instanceof String) {
            return (String) value;
        }
        return value.toString();
    }
    
    public String getString(String key, String defaultValue) {
        Object value = data.get(key);
        return value instanceof String ? (String) value : defaultValue;
    }
    
    public int getInt(String key) {
        Object value = data.get(key);
        if (value == null) {
            throw new RuntimeException("Key '" + key + "' not found");
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        throw new RuntimeException("Key '" + key + "' is not a number");
    }
    
    public int getInt(String key, int defaultValue) {
        Object value = data.get(key);
        return value instanceof Number ? ((Number) value).intValue() : defaultValue;
    }
    
    public long getLong(String key) {
        Object value = data.get(key);
        if (value == null) {
            throw new RuntimeException("Key '" + key + "' not found");
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        throw new RuntimeException("Key '" + key + "' is not a number");
    }
    
    public double getDouble(String key) {
        Object value = data.get(key);
        if (value == null) {
            throw new RuntimeException("Key '" + key + "' not found");
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        throw new RuntimeException("Key '" + key + "' is not a number");
    }
    
    public boolean getBoolean(String key) {
        Object value = data.get(key);
        if (value == null) {
            throw new RuntimeException("Key '" + key + "' not found");
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        throw new RuntimeException("Key '" + key + "' is not a boolean");
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = data.get(key);
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }
    
    public JSONObject getObject(String key) {
        Object value = data.get(key);
        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }
        throw new RuntimeException("Key '" + key + "' is not an object");
    }
    
    @SuppressWarnings("unchecked")
    public List<Object> getArray(String key) {
        Object value = data.get(key);
        if (value instanceof List) {
            return (List<Object>) value;
        }
        throw new RuntimeException("Key '" + key + "' is not an array");
    }
    
    public boolean has(String key) {
        return data.containsKey(key);
    }
    
    public boolean isNull(String key) {
        return data.containsKey(key) && data.get(key) == null;
    }
    
    // ==================== Setters ====================
    
    public JSONObject put(String key, Object value) {
        data.put(key, value);
        return this;
    }
    
    public JSONObject remove(String key) {
        data.remove(key);
        return this;
    }
    
    // ==================== Utility ====================
    
    public Set<String> keySet() {
        return data.keySet();
    }
    
    public int size() {
        return data.size();
    }
    
    public boolean isEmpty() {
        return data.isEmpty();
    }
    
    public Map<String, Object> toMap() {
        return new HashMap<>(data);
    }
    
    @Override
    public String toString() {
        return toJsonString();
    }
    
    public String toJsonString() {
        return serialize(data);
    }
    
    private String serialize(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        if (obj instanceof String) {
            return "\"" + escape((String) obj) + "\"";
        }
        
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        
        if (obj instanceof JSONObject) {
            return ((JSONObject) obj).toJsonString();
        }
        
        if (obj instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escape(entry.getKey().toString())).append("\":");
                sb.append(serialize(entry.getValue()));
                first = false;
            }
            
            sb.append("}");
            return sb.toString();
        }
        
        if (obj instanceof List) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            
            for (Object item : (List<?>) obj) {
                if (!first) sb.append(",");
                sb.append(serialize(item));
                first = false;
            }
            
            sb.append("]");
            return sb.toString();
        }
        
        return "\"" + escape(obj.toString()) + "\"";
    }
    
    private String escape(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    private static class ParseResult {
        final Object value;
        final int endIndex;
        
        ParseResult(Object value, int endIndex) {
            this.value = value;
            this.endIndex = endIndex;
        }
    }
}