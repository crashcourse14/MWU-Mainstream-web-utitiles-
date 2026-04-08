# MWU REST API Example

A complete REST API example demonstrating CRUD operations, middleware, and proper HTTP conventions.

## Features

- Full CRUD operations (Create, Read, Update, Delete)
- CORS support
- Rate limiting
- JSON request/response handling
- Proper HTTP status codes
- In-memory data storage

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | List all users |
| GET | `/api/users/:id` | Get specific user |
| POST | `/api/users` | Create new user |
| PUT | `/api/users/:id` | Update user |
| DELETE | `/api/users/:id` | Delete user |
| GET | `/health` | Health check |

## Running the Example

1. Compile the framework:
```bash
javac -cp "src/main/java" -d build src/main/java/com/mwu/*.java src/main/java/com/mwu/*/*.java
```

2. Compile and run:
```bash
javac -cp "build:src/main/java" examples/rest-api/src/RestApi.java
java -cp "build:examples/rest-api/src" examples.RestApi
```

## Testing the API

```bash
# Create a user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com"}'

# Get all users
curl http://localhost:8080/api/users

# Get specific user
curl http://localhost:8080/api/users/{id}

# Update user
curl -X PUT http://localhost:8080/api/users/{id} \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Doe"}'

# Delete user
curl -X DELETE http://localhost:8080/api/users/{id}
```