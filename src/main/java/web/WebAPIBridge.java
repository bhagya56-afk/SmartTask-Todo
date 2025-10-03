package web;

import models.*;
import auth.AuthManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WebAPIBridge - HTTP Server to connect Java backend with HTML frontend
 */
public class WebAPIBridge {
    private HttpServer server;
    private AuthManager authManager;
    private TaskManager taskManager;

    public WebAPIBridge(int port) throws IOException {
        this.authManager = new AuthManager();
        this.taskManager = new TaskManager();
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        setupRoutes();
    }

    private void setupRoutes() {
        // API routes
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/tasks", new TaskHandler());
        server.createContext("/api/tasks/add", new AddTaskHandler());
        server.createContext("/api/tasks/complete", new CompleteTaskHandler());
        server.createContext("/api/tasks/delete", new DeleteTaskHandler());
        server.createContext("/api/stats", new StatsHandler());

        // Static files
        server.createContext("/", new StaticFileHandler());
    }

    public void start() {
        server.setExecutor(null);
        server.start();
        System.out.println("=================================");
        System.out.println("SmartTask Web Server Started!");
        System.out.println("URL: http://localhost:8080");
        System.out.println("=================================");
    }

    // Login Handler
    class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);

            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequestBody(exchange);
                String email = params.get("email");
                String password = params.get("password");

                Student student = authManager.login(email, password);
                if (student != null) {
                    String response = "{\"success\":true,\"student\":" + student.toJson() + "}";
                    sendJsonResponse(exchange, 200, response);
                } else {
                    sendJsonResponse(exchange, 401, "{\"success\":false,\"error\":\"Invalid credentials\"}");
                }
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    // Register Handler
    class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);

            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequestBody(exchange);

                boolean success = authManager.register(
                        params.get("firstName"),
                        params.get("lastName"),
                        params.get("email"),
                        params.get("studentId"),
                        params.get("major"),
                        params.get("password")
                );

                if (success) {
                    sendJsonResponse(exchange, 201, "{\"success\":true,\"message\":\"Registration successful\"}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Registration failed - email may already exist\"}");
                }
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
            }
        }
    }

    // Task Handler (GET all tasks)
    class TaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);

            if ("GET".equals(exchange.getRequestMethod())) {
                String email = getQueryParam(exchange, "email");
                if (email != null) {
                    List<Task> tasks = taskManager.getTasksByStudent(email);
                    StringBuilder jsonResponse = new StringBuilder("[");
                    for (int i = 0; i < tasks.size(); i++) {
                        jsonResponse.append(tasks.get(i).toJson());
                        if (i < tasks.size() - 1) {
                            jsonResponse.append(",");
                        }
                    }
                    jsonResponse.append("]");
                    sendJsonResponse(exchange, 200, jsonResponse.toString());
                } else {
                    sendJsonResponse(exchange, 400, "{\"error\":\"Email parameter required\"}");
                }
            }
        }
    }

    // Add Task Handler
    class AddTaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);

            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequestBody(exchange);

                try {
                    String dueDateStr = params.get("dueDate");
                    String dueTimeStr = params.get("dueTime");

                    LocalDateTime dueDate;
                    if (dueTimeStr != null && !dueTimeStr.isEmpty()) {
                        dueDate = LocalDateTime.parse(dueDateStr + "T" + dueTimeStr + ":00");
                    } else {
                        dueDate = LocalDateTime.parse(dueDateStr + "T23:59:59");
                    }

                    Task newTask = taskManager.addTask(
                            params.get("title"),
                            params.get("description"),
                            params.get("category"),
                            Task.Priority.fromString(params.get("priority")),
                            dueDate,
                            params.get("studentEmail")
                    );

                    sendJsonResponse(exchange, 201, "{\"success\":true,\"task\":" + newTask.toJson() + "}");
                } catch (Exception e) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}");
                }
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
            }
        }
    }

    // Complete Task Handler
    class CompleteTaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);

            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequestBody(exchange);
                int taskId = Integer.parseInt(params.get("taskId"));

                if (taskManager.completeTask(taskId)) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Task completed\"}");
                } else {
                    sendJsonResponse(exchange, 404, "{\"success\":false,\"error\":\"Task not found\"}");
                }
            }
        }
    }

    // Delete Task Handler
    class DeleteTaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);

            if ("DELETE".equals(exchange.getRequestMethod())) {
                String taskIdStr = getQueryParam(exchange, "id");
                if (taskIdStr != null) {
                    int taskId = Integer.parseInt(taskIdStr);
                    if (taskManager.deleteTask(taskId)) {
                        sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Task deleted\"}");
                    } else {
                        sendJsonResponse(exchange, 404, "{\"success\":false,\"error\":\"Task not found\"}");
                    }
                }
            }
        }
    }

    // Stats Handler
    class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);

            if ("GET".equals(exchange.getRequestMethod())) {
                String email = getQueryParam(exchange, "email");
                if (email != null) {
                    TaskManager.TaskStats stats = taskManager.getTaskStats(email);
                    sendJsonResponse(exchange, 200, stats.toJson());
                }
            }
        }
    }

    // Static File Handler
    class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path)) path = "/index.html";

            File file = new File("web" + path);
            if (file.exists() && file.isFile()) {
                String contentType = getContentType(path);
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, file.length());

                try (FileInputStream fis = new FileInputStream(file);
                     OutputStream os = exchange.getResponseBody()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                sendResponse(exchange, 404, "File not found: " + path);
            }
        }
    }

    // Helper methods
    private void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private Map<String, String> parseRequestBody(HttpExchange exchange) throws IOException {
        Map<String, String> params = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }

            String bodyStr = body.toString();
            if (bodyStr.startsWith("{") && bodyStr.endsWith("}")) {
                // Simple JSON parsing
                bodyStr = bodyStr.substring(1, bodyStr.length() - 1);
                String[] pairs = bodyStr.split(",");
                for (String pair : pairs) {
                    String[] kv = pair.split(":");
                    if (kv.length == 2) {
                        String key = kv[0].replace("\"", "").trim();
                        String value = kv[1].replace("\"", "").trim();
                        params.put(key, value);
                    }
                }
            }
        }
        return params;
    }

    private String getQueryParam(HttpExchange exchange, String paramName) {
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2 && kv[0].equals(paramName)) {
                    return kv[1];
                }
            }
        }
        return null;
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, statusCode, response);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".json")) return "application/json";
        return "text/plain";
    }

    // Main method
    public static void main(String[] args) {
        try {
            WebAPIBridge server = new WebAPIBridge(8080);
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}
