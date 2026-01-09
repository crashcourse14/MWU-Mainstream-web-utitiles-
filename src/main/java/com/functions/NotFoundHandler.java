package com.functions;


import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;

/** Handles custom 404 pages */
public class NotFoundHandler {

    private String htmlPage; // optional static page content

    /** Constructor with HTML content */
    public NotFoundHandler(String htmlPage) {
        this.htmlPage = htmlPage;
    }

    /** Default constructor (dynamic 404) */
    public NotFoundHandler() {
        this.htmlPage = null;
    }

    /** Main method called by MWU */
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/html");
        byte[] bytes;

        if (htmlPage != null) {
            bytes = htmlPage.getBytes();
        } else {
            String defaultHtml = "<h1>404 Not Found</h1><p>The requested page does not exist.</p>";
            bytes = defaultHtml.getBytes();
        }

        exchange.sendResponseHeaders(404, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
