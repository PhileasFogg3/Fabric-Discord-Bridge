package org.phileasfogg3.fabricdiscordbridge.discord;

import net.dv8tion.jda.api.utils.data.DataObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DiscordWebhookSender {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static void send(
            String webhookUrl,
            String username,
            String avatarUrl,
            String content
    ) {
        try {
            String json = DataObject.empty()
                    .put("username", username)
                    .put("avatar_url", avatarUrl)
                    .put("content", content)
                    .toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

