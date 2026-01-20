package com.functions;

import com.mwu.MWU;
import com.mwu.logger.Logger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.utils.TrafficMonitor.TrafficStats;
import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class StatsHandler implements HttpHandler {
    private final MWU mwu;
    private final Logger logger = new Logger();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsHandler(MWU mwu) {
        this.mwu = mwu;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        long startTime = System.currentTimeMillis();

        // Only allow GET requests
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            long responseTime = System.currentTimeMillis() - startTime;
            if (mwu.getTrafficMonitor() != null) {
                mwu.getTrafficMonitor().recordResponse(405, responseTime, 0);
            }
            return;
        }

        // Check if traffic monitoring is enabled
        if (!isTrafficMonitoringEnabled()) {
            String errorHtml = generateErrorHtml("Traffic monitoring is disabled");
            byte[] responseBytes = errorHtml.getBytes("UTF-8");
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(403, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
            long responseTime = System.currentTimeMillis() - startTime;
            if (mwu.getTrafficMonitor() != null) {
                mwu.getTrafficMonitor().recordResponse(403, responseTime, responseBytes.length);
            }
            return;
        }

        TrafficStats stats = mwu.getTrafficStats();
        String html = generateStatsHtml(stats);

        byte[] responseBytes = html.getBytes("UTF-8");
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }

        long responseTime = System.currentTimeMillis() - startTime;
        mwu.getTrafficMonitor().recordResponse(200, responseTime, responseBytes.length);

        logger.info("Served traffic statistics page");
    }

    private boolean isTrafficMonitoringEnabled() {
        try {
            // We need to check settings, but we don't have direct access to Settings object
            // For now, assume it's enabled if traffic monitor exists
            return mwu.getTrafficMonitor() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private String generateStatsHtml(TrafficStats stats) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>MWU Traffic Statistics</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; max-width: 1200px; margin: 0 auto; padding: 20px; background-color: #f5f5f5; }\n");
        html.append("        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; text-align: center; }\n");
        html.append("        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; margin-bottom: 20px; }\n");
        html.append("        .stat-card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n");
        html.append("        .stat-card h3 { margin-top: 0; color: #333; border-bottom: 2px solid #667eea; padding-bottom: 10px; }\n");
        html.append("        .metric { display: flex; justify-content: space-between; margin: 10px 0; padding: 5px 0; border-bottom: 1px solid #eee; }\n");
        html.append("        .metric:last-child { border-bottom: none; }\n");
        html.append("        .metric-value { font-weight: bold; color: #667eea; }\n");
        html.append("        .performance-good { color: #28a745; }\n");
        html.append("        .performance-warning { color: #ffc107; }\n");
        html.append("        .performance-danger { color: #dc3545; }\n");
        html.append("        .list-item { display: flex; justify-content: space-between; padding: 5px 0; border-bottom: 1px solid #f0f0f0; }\n");
        html.append("        .refresh-btn { background: #667eea; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; font-size: 16px; margin: 20px 0; }\n");
        html.append("        .refresh-btn:hover { background: #5a6fd8; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"header\">\n");
        html.append("        <h1>MWU Web Server Traffic Statistics</h1>\n");
        html.append("        <p>Server started: ").append(stats.startTime.format(formatter)).append("</p>\n");
        html.append("    </div>\n");
        html.append("\n");
        html.append("    <button class=\"refresh-btn\" onclick=\"location.reload()\">Refresh Statistics</button>\n");
        html.append("\n");
        html.append("    <div class=\"stats-grid\">\n");
        html.append("        <div class=\"stat-card\">\n");
        html.append("            <h3>📊 Request Overview</h3>\n");
        html.append("            <div class=\"metric\"><span>Total Requests:</span><span class=\"metric-value\">").append(stats.totalRequests).append("</span></div>\n");
        html.append("            <div class=\"metric\"><span>Unique IPs:</span><span class=\"metric-value\">").append(stats.uniqueIPs).append("</span></div>\n");
        html.append("            <div class=\"metric\"><span>Active Connections:</span><span class=\"metric-value\">").append(stats.activeConnections).append("</span></div>\n");
        html.append("            <div class=\"metric\"><span>Peak Connections:</span><span class=\"metric-value\">").append(stats.peakConnections).append("</span></div>\n");
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"stat-card\">\n");
        html.append("            <h3>⚡ Performance Metrics</h3>\n");
        html.append("            <div class=\"metric\"><span>Avg Response Time:</span><span class=\"metric-value\">").append(formatTime(stats.avgResponseTime)).append("</span></div>\n");
        html.append("            <div class=\"metric\"><span>Min Response Time:</span><span class=\"metric-value\">").append(formatTime(stats.minResponseTime)).append("</span></div>\n");
        html.append("            <div class=\"metric\"><span>Max Response Time:</span><span class=\"metric-value\">").append(formatTime(stats.maxResponseTime)).append("</span></div>\n");
        html.append("            <div class=\"metric\"><span>Median Response Time:</span><span class=\"metric-value\">").append(formatTime(stats.medianResponseTime)).append("</span></div>\n");
        html.append("            <div class=\"metric\"><span>95th Percentile:</span><span class=\"metric-value\">").append(formatTime(stats.p95ResponseTime)).append("</span></div>\n");
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"stat-card\">\n");
        html.append("            <h3>📈 Data Transfer</h3>\n");
        html.append("            <div class=\"metric\"><span>Total Bytes Sent:</span><span class=\"metric-value\">").append(formatBytes(stats.totalBytesSent)).append("</span></div>\n");
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"stat-card\">\n");
        html.append("            <h3>🛣️ Top Requested Paths</h3>\n");
        if (stats.topPaths.isEmpty()) {
            html.append("            <p>No data available</p>\n");
        } else {
            for (Map.Entry<String, Long> entry : stats.topPaths.entrySet()) {
                html.append("            <div class=\"list-item\"><span>").append(entry.getKey()).append("</span><span>").append(entry.getValue()).append("</span></div>\n");
            }
        }
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"stat-card\">\n");
        html.append("            <h3>📋 Request Methods</h3>\n");
        if (stats.requestsByMethod.isEmpty()) {
            html.append("            <p>No data available</p>\n");
        } else {
            for (Map.Entry<String, Long> entry : stats.requestsByMethod.entrySet()) {
                html.append("            <div class=\"list-item\"><span>").append(entry.getKey()).append("</span><span>").append(entry.getValue()).append("</span></div>\n");
            }
        }
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"stat-card\">\n");
        html.append("            <h3>📊 Response Status Codes</h3>\n");
        if (stats.requestsByStatusCode.isEmpty()) {
            html.append("            <p>No data available</p>\n");
        } else {
            for (Map.Entry<Integer, Long> entry : stats.requestsByStatusCode.entrySet()) {
                html.append("            <div class=\"list-item\"><span>").append(entry.getKey()).append("</span><span>").append(entry.getValue()).append("</span></div>\n");
            }
        }
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("\n");
        html.append("    <script>\n");
        html.append("        function formatBytes(bytes) {\n");
        html.append("            if (bytes === 0) return '0 Bytes';\n");
        html.append("            const k = 1024;\n");
        html.append("            const sizes = ['Bytes', 'KB', 'MB', 'GB'];\n");
        html.append("            const i = Math.floor(Math.log(bytes) / Math.log(k));\n");
        html.append("            return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];\n");
        html.append("        }\n");
        html.append("        function formatTime(ms) {\n");
        html.append("            if (ms < 1000) return Math.round(ms) + 'ms';\n");
        html.append("            return (ms / 1000).toFixed(2) + 's';\n");
        html.append("        }\n");
        html.append("    </script>\n");
        html.append("</body>\n");
        html.append("</html>\n");

        return html.toString();
    }

    private String generateErrorHtml(String message) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Access Denied</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; text-align: center; padding: 50px; background-color: #f5f5f5; }\n");
        html.append("        .error { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); display: inline-block; }\n");
        html.append("        h1 { color: #dc3545; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"error\">\n");
        html.append("        <h1>Access Denied</h1>\n");
        html.append("        <p>").append(message).append("</p>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        return html.toString();
    }

    private String formatBytes(long bytes) {
        if (bytes == 0) return "0 Bytes";
        final String[] units = new String[] { "Bytes", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    private String formatTime(double ms) {
        if (ms < 1000) return String.format("%.0fms", ms);
        return String.format("%.2fs", ms / 1000);
    }
}