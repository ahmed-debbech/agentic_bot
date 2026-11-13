package com.debbech.tasks.hackernews;

import com.debbech.tasks.AgentTask;
import com.debbech.tasks.hackernews.exceptions.EndLoopException;
import com.debbech.tasks.hackernews.tools.CurlLinkTool;
import com.debbech.tasks.hackernews.tools.Tool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class HackerNewsTask implements AgentTask {


    @Override
    public String behaveOnAiResponse(String aiResponse) throws EndLoopException {

        String nextPrompt = "";
        //check for tools tags
        //should match this type -%%tool_name%%-{parameters}
        Tool tool = getToolCall(aiResponse);

        switch(tool.tool_name){
            case "curl_link":
                log.info("Using tool curl_link");
                String body = "";
                try {
                    body = CurlLinkTool.getResult(tool.tool_params);
                } catch (Exception e) {
                    log.warn("error occurred after calling the tool because: {}", e.getMessage());
                    nextPrompt += "+%%curl_link%%+\n" + "PASS";
                }
                nextPrompt += "+%%curl_link%%+\n" + body;
                break;
        }
        return nextPrompt;
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

    private Tool getToolCall(String aiResponse){
        Pattern pattern = Pattern.compile("-%%(.*?)%%-\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(aiResponse);
        if (!matcher.find()) {
            log.error("could not find tool call for the the AI response {}", aiResponse);
            throw new EndLoopException("could not find tool call for the AI response");
        }
        Tool t = new Tool();
        t.tool_call = matcher.group(0);
        t.tool_name = matcher.group(1);
        t.tool_params = matcher.group(2);

        log.info("found a tool call {}", t.tool_call);
        return t;
    }

}
