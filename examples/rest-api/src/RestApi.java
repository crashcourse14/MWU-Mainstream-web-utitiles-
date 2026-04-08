package examples;

import com.mwu.MWU;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST API Example for MWU Framework
 *
 * Demonstrates building a complete REST API with CRUD operations,
 * middleware, and proper HTTP status codes.
 */
public class RestApi {
    // Simple in-memory "database"
    private static final Map<String, Map<String, Object>> users = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        MWU server = MWU.builder()
            .port(8080)
            .host("0.0.0.0")
            .publicDirectory("../../public")
            .enableCors()
            .rateLimit(100)
            .build();

        // Middleware to add JSON content type for API routes
        server.use("/api", (req, res, next) -> {
            res.header("Content-Type", "application/json");
            next.run();
        });

        // GET /api/users - List all users
        server.get("/api/users", (req, res) -> {
            res.json(new ArrayList<>(users.values()));
        });

        // GET /api/users/:id - Get specific user
        server.get("/api/users/:id", (req, res) -> {
            String id = req.getPathParam("id");
            Map<String, Object> user = users.get(id);

            if (user == null) {
                res.status(404).json(Map.of("error", "User not found"));
                return;
            }

            res.json(user);
        });

        // POST /api/users - Create new user
        server.post("/api/users", (req, res) -> {
            try {
                // Parse JSON body (simplified)
                String body = req.getBodyAsString();
                Map<String, Object> newUser = parseJsonBody(body);

                String id = UUID.randomUUID().toString();
                newUser.put("id", id);
                newUser.put("createdAt", System.currentTimeMillis());

                users.put(id, newUser);

                res.status(201).json(newUser);
            } catch (Exception e) {
                res.status(400).json(Map.of("error", "Invalid JSON"));
            }
        });

        // PUT /api/users/:id - Update user
        server.put("/api/users/:id", (req, res) -> {
            String id = req.getPathParam("id");

            if (!users.containsKey(id)) {
                res.status(404).json(Map.of("error", "User not found"));
                return;
            }

            try {
                String body = req.getBodyAsString();
                Map<String, Object> updates = parseJsonBody(body);

                Map<String, Object> user = users.get(id);
                user.putAll(updates);
                user.put("updatedAt", System.currentTimeMillis());

                res.json(user);
            } catch (Exception e) {
                res.status(400).json(Map.of("error", "Invalid JSON"));
            }
        });

        // DELETE /api/users/:id - Delete user
        server.delete("/api/users/:id", (req, res) -> {
            String id = req.getPathParam("id");

            if (users.remove(id) == null) {
                res.status(404).json(Map.of("error", "User not found"));
                return;
            }

            res.status(204).send(""); // No content
        });

        // Health check
        server.get("/health", (req, res) -> {
            res.json(Map.of(
                "status", "healthy",
                "users", users.size(),
                "timestamp", System.currentTimeMillis()
            ));
        });

        try {
            server.start();
            System.out.println("REST API server started on http://localhost:8080");
            System.out.println("API endpoints:");
            System.out.println("  GET    /api/users");
            System.out.println("  GET    /api/users/:id");
            System.out.println("  POST   /api/users");
            System.out.println("  PUT    /api/users/:id");
            System.out.println("  DELETE /api/users/:id");
            System.out.println("  GET    /health");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Simple JSON parser (for demonstration - in real app use a proper JSON library)
    private static Map<String, Object> parseJsonBody(String body) {
        Map<String, Object> result = new HashMap<>();
        if (body == null || body.trim().isEmpty()) return result;

        // Very basic parsing - assumes simple {"key":"value"} format
        body = body.trim();
        if (body.startsWith("{") && body.endsWith("}")) {
            body = body.substring(1, body.length() - 1);
            String[] pairs = body.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replaceAll("\"", "");
                    String value = keyValue[1].trim().replaceAll("\"", "");
                    result.put(key, value);
                }
            }
        }
        return result;
    }
}