package examples;

import com.mwu.MWU;

/**
 * Hello World Example for MWU Framework
 *
 * This example demonstrates the basic usage of MWU Framework
 * to create a simple web server with a few routes.
 */
public class HelloWorld {
    public static void main(String[] args) {
        MWU server = MWU.builder()
            .port(8080)
            .host("0.0.0.0")
            .publicDirectory("../../public") // Use the shared public directory
            .build();

        // Root route
        server.get("/", (req, res) -> {
            res.html("<h1>Hello, MWU World!</h1><p>Welcome to MWU Framework</p>");
        });

        // API route with query parameter
        server.get("/api/greet", (req, res) -> {
            String name = req.getQueryParam("name", "World");
            res.json(java.util.Map.of("message", "Hello, " + name + "!"));
        });

        // Route with path parameter
        server.get("/user/:id", (req, res) -> {
            String userId = req.getPathParam("id");
            res.json(java.util.Map.of(
                "userId", userId,
                "name", "User " + userId
            ));
        });

        try {
            server.start();
            System.out.println("Hello World server started on http://localhost:8080");
            System.out.println("Try: http://localhost:8080/api/greet?name=Java");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}