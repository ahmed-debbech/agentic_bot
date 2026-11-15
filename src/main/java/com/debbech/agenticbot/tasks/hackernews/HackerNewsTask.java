package com.debbech.agenticbot.tasks.hackernews;

import com.debbech.agenticbot.tasks.AgentTask;
import com.debbech.agenticbot.tasks.hackernews.tools.CurlLinkTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class HackerNewsTask implements AgentTask {

    @Override
    public List<Object> tools() {
        return List.of(new CurlLinkTool());
    }

    @Override
    public String initialPrompt() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        // Fetch story IDs
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://hacker-news.firebaseio.com/v0/topstories.json"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        ObjectMapper mapper = new ObjectMapper();
        JsonNode ids = mapper.readTree(response.body());

        StringBuilder stories = new StringBuilder("[");

        // Fetch first 20 stories
        for (int i = 0; i < Math.min(20, ids.size()); i++) {

            String id = ids.get(i).asText();

            HttpRequest storyRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://hacker-news.firebaseio.com/v0/item/" + id + ".json"))
                    .GET()
                    .build();

            HttpResponse<String> storyResponse = client.send(
                    storyRequest, HttpResponse.BodyHandlers.ofString()
            );

            if (i > 0) {
                stories.append(",");
            }

            stories.append(storyResponse.body());
        }

        stories.append("]");

        String initPrompt = "";

        try {
            initPrompt = Files.readString(
                    Path.of("/prompts/skill_hackernews.md"),
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {
            log.error("NO system prompt found to begin with.");
            throw new RuntimeException(e);
        }
        return initPrompt + " " + stories.toString();
    }

}
