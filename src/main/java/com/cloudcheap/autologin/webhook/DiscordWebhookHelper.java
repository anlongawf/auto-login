package com.cloudcheap.autologin.webhook;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;

public class DiscordWebhookHelper {
    private final String url;
    private final HttpClient httpClient;

    public DiscordWebhookHelper(String url) {
        this.url = url;
        this.httpClient = HttpClient.newHttpClient();
    }

    public void sendNotification(String playerName, String ip, boolean success) {
        if (url == null || url.isEmpty() || !url.startsWith("http")) return;

        String description = success 
            ? "Player **" + playerName + "** has been automatically logged in."
            : "Player **" + playerName + "** requires manual login.";

        String json = "{"
                + "\"embeds\": [{"
                + "\"title\": \"🛡️ AutoLogin Alert\","
                + "\"description\": \"" + description + "\","
                + "\"color\": " + (success ? "65280" : "16711680") + "," // Green / Red
                + "\"fields\": ["
                + "{\"name\": \"Player Name\", \"value\": \"" + playerName + "\", \"inline\": true},"
                + "{\"name\": \"IP Address\", \"value\": \"" + ip + "\", \"inline\": true}"
                + "],"
                + "\"timestamp\": \"" + java.time.Instant.now().toString() + "\""
                + "}]"
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .exceptionally(ex -> {
                    // Do not spam console, but log warning if helpful
                    return null;
                });
    }
}
