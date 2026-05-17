package com.procurement.frontend.client;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;
import java.util.function.Consumer;

public class APIClient {

    private static String baseUrl = "http://localhost:8080";
    private static final String CONFIG_FILE = "config.properties";
    private final HttpClient client;

    static {
        loadConfig();
    }

    public APIClient() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static void setBaseUrl(String url) {
        baseUrl = url;
        saveConfig();
    }

    private static void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                Properties prop = new Properties();
                prop.load(input);
                String url = prop.getProperty("api.base_url");
                if (url != null && !url.trim().isEmpty()) {
                    baseUrl = url.trim();
                }
            } catch (IOException ex) {
                System.err.println("Error loading config.properties: " + ex.getMessage());
            }
        }
    }

    private static void saveConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            Properties prop = new Properties();
            prop.setProperty("api.base_url", baseUrl);
            prop.store(output, "ProcureFlow Client Configuration");
        } catch (IOException io) {
            System.err.println("Error saving config.properties: " + io.getMessage());
        }
    }

    public void getAsync(String endpoint, Consumer<HttpResponse<String>> callback) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + endpoint))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(callback)
                    .exceptionally(ex -> {
                        System.err.println("API Request Error to " + endpoint + ": " + ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void postAsync(String endpoint, String jsonBody, Consumer<HttpResponse<String>> callback) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + endpoint))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(callback)
                    .exceptionally(ex -> {
                        System.err.println("API POST Request Error to " + endpoint + ": " + ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void putAsync(String endpoint, String jsonBody, Consumer<HttpResponse<String>> callback) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + endpoint))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(callback)
                    .exceptionally(ex -> {
                        System.err.println("API PUT Request Error to " + endpoint + ": " + ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void patchAsync(String endpoint, String jsonBody, Consumer<HttpResponse<String>> callback) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + endpoint))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(callback)
                    .exceptionally(ex -> {
                        System.err.println("API PATCH Request Error to " + endpoint + ": " + ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAsync(String endpoint, Consumer<HttpResponse<String>> callback) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + endpoint))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .DELETE()
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(callback)
                    .exceptionally(ex -> {
                        System.err.println("API DELETE Request Error to " + endpoint + ": " + ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
