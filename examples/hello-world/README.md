# MWU Hello World Example

This is a simple example demonstrating the basic features of the MWU Framework.

## Running the Example

1. Compile the framework (from the project root):
```bash
javac -cp "src/main/java" -d build src/main/java/com/mwu/*.java src/main/java/com/mwu/*/*.java
```

2. Compile and run the example:
```bash
javac -cp "build:src/main/java" examples/hello-world/src/HelloWorld.java
java -cp "build:examples/hello-world/src" examples.HelloWorld
```

3. Open your browser to http://localhost:8080

## What it does

- Serves a simple HTML page at the root
- Provides a JSON API at `/api/greet` with query parameters
- Demonstrates path parameters at `/user/:id`

## Features Demonstrated

- Fluent builder API
- Basic routing (GET)
- HTML and JSON responses
- Query and path parameters