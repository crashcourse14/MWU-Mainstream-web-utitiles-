# MWU Framework 🚀

**MWU (Mainstream Web Utilities)** is a modern, lightweight HTTP server framework for Java that makes building web applications simple and elegant. Built with performance, security, and developer experience in mind.

[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## ✨ Key Features

- **🚀 Fluent Builder API** - Chain configuration methods for clean, readable code
- **🔧 Middleware Pipeline** - Powerful request/response processing with CORS, rate limiting, and security headers
- **🛣️ Advanced Routing** - RESTful routing with parameter extraction and pattern matching
- **⚡ High Performance** - Non-blocking I/O with configurable thread pools
- **🔒 Built-in Security** - Security headers, rate limiting, and request validation
- **📊 Monitoring & Metrics** - Real-time traffic monitoring and health checks
- **🛠️ Maintenance Mode** - Graceful maintenance mode with custom HTML pages
- **📁 Static File Serving** - Automatic serving of static files with proper MIME types
- **🎨 Custom Error Pages** - Beautiful 404.html and maintenance.html support

## 🏁 Quick Start

### Prerequisites
- Java 17 or higher
- Basic understanding of Java and HTTP

### Installation

1. Clone the repository:
```bash
git clone https://github.com/crashcourse14/MWU-Mainstream-web-utitiles-.git
cd MWU-Mainstream-web-utitiles-
```

2. Compile the framework:
```bash
javac -cp "src/main/java" -d build src/main/java/com/mwu/*.java src/main/java/com/mwu/*/*.java
```

3. Run a hello world example:
```bash
javac -cp "build:src/main/java" examples/hello-world/src/HelloWorld.java
java -cp "build:examples/hello-world/src" examples.HelloWorld
```

The server will start on `http://localhost:8080`!

## 📖 Examples

Check out the `examples/` directory for complete working examples:

- **[Hello World](examples/hello-world/)** - Basic server setup with routing
- **[REST API](examples/rest-api/)** - Full CRUD API with middleware
- **[Basic Server](examples/basic-server/)** - Comprehensive example with all features

Each example includes a README with detailed instructions.

## 📖 Basic Usage

### Simple Server Setup

```java
import com.mwu.MWU;

public class MyServer {
    public static void main(String[] args) {
        MWU server = MWU.builder()
            .port(8080)
            .host("0.0.0.0")
            .publicDirectory("public")
            .build();

        // Add a simple route
        server.get("/", (req, res) -> {
            res.html("<h1>Hello, MWU!</h1>");
        });

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### REST API Example

```java
MWU server = MWU.builder()
    .port(8080)
    .enableCors()
    .rateLimit(100) // 100 requests per minute
    .build();

// Basic routes
server.get("/api/users", (req, res) -> {
    res.json(Arrays.asList(
        Map.of("id", 1, "name", "Alice"),
        Map.of("id", 2, "name", "Bob")
    ));
});

server.post("/api/users", (req, res) -> {
    // Handle user creation
    res.status(201).json(Map.of("message", "User created"));
});

// Parameterized routes
server.get("/api/users/:id", (req, res) -> {
    String userId = req.getParams().get("id");
    res.json(Map.of("id", userId, "name", "User " + userId));
});

server.start();
```

## 🎛️ Advanced Features

### Middleware Pipeline

MWU supports a powerful middleware system for processing requests:

```java
server.use((req, res, next) -> {
    // Custom middleware
    System.out.println("Request: " + req.getPath());
    next.run();
});

// Built-in middleware
server.use(new LoggingMiddleware());
server.use(new SecurityHeadersMiddleware());
server.use(new RateLimitMiddleware(100));
```

### Maintenance Mode

Enable maintenance mode programmatically or via `settings.json`:

```java
// Enable maintenance mode
server.enableMaintenance();

// Set custom maintenance message
server.setMaintenanceMessage("Scheduled maintenance in progress");

// Check maintenance status
if (server.getMaintenanceManager().isMaintenanceModeEnabled()) {
    System.out.println("Server is in maintenance mode");
}
```

**settings.json:**
```json
{
  "maintenance": true,
  "port": 8080,
  "host": "localhost",
  "trafficMonitoring": true
}
```

### Terminal Control

Control your server via terminal commands while it's running:

```bash
# Start the server
java -cp "src/main/java" com.server.ModernServer

# In another terminal, control the server:
stop     # Stop the server gracefully
restart  # Restart the server
status   # Show server status
help     # Show available commands
```

### Auto-Restart

Schedule automatic server restarts:

```java
// Restart in 5 minutes
server.restartIn(300);

// Cancel scheduled restart
server.cancelRestart();
```

### Static File Serving

MWU automatically serves static files from the `public/` directory:

```
public/
├── index.html      # Served at /
├── 404.html        # Custom 404 page
├── maintenance.html # Maintenance mode page
├── css/
│   └── style.css
└── js/
    └── app.js
```

### Health Checks & Metrics

Built-in monitoring endpoints:

```bash
# Health check
curl http://localhost:8080/_health

# Metrics
curl http://localhost:8080/_metrics

# Server info
curl http://localhost:8080/_info
```

### Real-Time Stats Dashboard

MWU includes a live statistics page at `/stats.html` that polls the server every few seconds and renders real-time traffic and performance data.

- **Total requests**
- **Unique IPs**
- **Active and peak connections**
- **Response time distribution**
- **Top requested paths**
- **Request methods breakdown**
- **Response status code counts**
- **Total bytes sent**

To use it, make sure the `public/stats.html` file exists and then open:

```bash
http://localhost:8080/stats.html
```

The dashboard fetches from `/ _metrics` internally, so the endpoint must be accessible for live updates.

## ⚙️ Configuration

### Builder Pattern

```java
MWU server = MWU.builder()
    .port(8080)
    .host("0.0.0.0")
    .publicDirectory("public")
    .threadPoolSize(100)
    .enableCors()
    .rateLimit(1000)
    .config(MWUConfig.builder()
        .trafficMonitoringEnabled(true)
        .maxRequestSize(10 * 1024 * 1024) // 10MB
        .errorHandler((req, err) -> {
            System.err.println("Error: " + err.getMessage());
        })
        .build())
    .build();
```

### Settings File

Create a `settings.json` file in the root directory:

```json
{
  "maintenance": false,
  "port": 8080,
  "host": "localhost",
  "trafficMonitoring": true
}
```

## 🛠️ API Reference

### HTTP Methods

```java
server.get(path, handler);
server.post(path, handler);
server.put(path, handler);
server.delete(path, handler);
server.patch(path, handler);
server.options(path, handler);
```

### Route Handlers

```java
(req, res) -> {
    // Access request data
    String path = req.getPath();
    String method = req.getMethod();
    Map<String, String> params = req.getParams();
    String body = req.getBody();

    // Send responses
    res.status(200).send("OK");
    res.json(Map.of("key", "value"));
    res.html("<h1>Hello</h1>");
}
```

### Response Methods

```java
res.status(200);           // Set status code
res.header("key", "value"); // Set header
res.send("text");          // Send text response
res.json(object);          // Send JSON response
res.html("<html>");        // Send HTML response
```

## 🚀 Deployment

### Running in Production

1. Build the framework:
```bash
javac -cp "src/main/java" -d build src/main/java/com/mwu/*.java src/main/java/com/mwu/*/*.java
```

2. Build your application:
```bash
javac -cp "build:src/main/java" -d app examples/hello-world/src/HelloWorld.java
```

3. Run with production settings:
```bash
java -cp "build:app" \
  -Djava.awt.headless=true \
  -Xmx512m \
  examples.HelloWorld
```

### Docker Support

Create a `Dockerfile`:

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY . /app
RUN javac -cp "src/main/java" -d build src/main/java/com/mwu/*.java src/main/java/com/mwu/*/*.java
RUN javac -cp "build:src/main/java" examples/hello-world/src/HelloWorld.java
EXPOSE 8080
CMD ["java", "-cp", "build:examples/hello-world/src", "examples.HelloWorld"]
```

## 🤝 Contributing

We welcome contributions! Here's how to get started:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Make your changes and add tests
4. Run the server to ensure it works
5. Commit your changes: `git commit -am 'Add feature'`
6. Push to the branch: `git push origin feature-name`
7. Submit a pull request

### Development Setup

```bash
# Clone and setup
git clone https://github.com/crashcourse14/MWU-Mainstream-web-utitiles-.git
cd MWU-Mainstream-web-utitiles-

# Build the framework
javac -cp "src/main/java" -d build src/main/java/com/mwu/*.java src/main/java/com/mwu/*/*.java

# Run an example
javac -cp "build:src/main/java" examples/hello-world/src/HelloWorld.java
java -cp "build:examples/hello-world/src" examples.HelloWorld
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Built with ❤️ for the Java community
- Inspired by modern web frameworks
- Special thanks to all contributors

---

**Happy coding with MWU! 🎉**


