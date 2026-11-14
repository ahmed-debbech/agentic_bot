package com.debbech.tasks.hackernews.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
public class CurlLinkTool {

    @Tool("Fetches a web page and returns its visible text content")
    public String curl_link(@P("The absolute URL to fetch") String url) {

        try {
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
                log.warn("Failed to fetch HTML for {}. HTTP status: {}", url, statusCode);
                return "PASS";
            }

            Document doc = Jsoup.parse(response.body());
            return doc.body().text();
        } catch (Exception e) {
            log.warn("error occurred while calling curl_link for {} because: {}", url, e.getMessage());
            return "PASS";
        }
    }

}
