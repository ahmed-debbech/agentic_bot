package com.debbech.tasks.hackernews.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class CurlLinkTool {

    public static String getResult(String url) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        int statusCode = response.statusCode();

        if (statusCode < 200 || statusCode >= 300) {
            throw new IOException(
                    "Failed to fetch HTML. HTTP status: " + statusCode
            );
        }

        Document doc = Jsoup.parse(response.body());
        String text = doc.body().text();

        return text;
    }

}
