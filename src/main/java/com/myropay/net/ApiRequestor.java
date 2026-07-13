package com.myropay.net;

import com.myropay.Myropay;
import com.myropay.exception.ApiException;
import com.myropay.exception.InvalidRequestException;
import com.myropay.exception.MyropayException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

public final class ApiRequestor {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private ApiRequestor() {}

    public static Map<String, Object> request(String method, Map<String, String> query, Map<String, Object> body, String apiKeyOverride)
            throws MyropayException {
        String key = apiKeyOverride != null ? apiKeyOverride : Myropay.apiKey;
        if (key == null || key.isEmpty()) {
            throw new InvalidRequestException(
                    "No API key provided. Set Myropay.apiKey = \"sk_test_...\" before making a request, "
                    + "or pass an apiKey directly to the call.");
        }

        StringBuilder url = new StringBuilder(Myropay.API_BASE);
        if (query != null && !query.isEmpty()) {
            url.append('?');
            boolean first = true;
            for (Map.Entry<String, String> e : query.entrySet()) {
                if (!first) url.append('&');
                first = false;
                url.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8))
                   .append('=')
                   .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
            }
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url.toString()))
                .timeout(Duration.ofSeconds(30))
                .header("X-API-Key", key)
                .header("Content-Type", "application/json")
                .header("User-Agent", "myropay-java/" + Myropay.VERSION);

        if ("POST".equals(method)) {
            String payload = body != null ? Json.write(body) : "{}";
            builder.POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));
        } else {
            builder.GET();
        }

        HttpResponse<String> res;
        try {
            res = CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new ApiException("Could not reach the MyroPay API: " + e.getMessage(), null, null);
        }

        Map<String, Object> data;
        try {
            data = Json.parseObject(res.body());
        } catch (Exception e) {
            throw new ApiException("Invalid (non-JSON) response from the MyroPay API", res.statusCode(), null);
        }

        Object success = data.get("success");
        if (res.statusCode() >= 400 || Boolean.FALSE.equals(success)) {
            String message = data.get("message") != null ? String.valueOf(data.get("message")) : "MyroPay API request failed";
            throw new ApiException(message, res.statusCode(), data);
        }

        return data;
    }
}
