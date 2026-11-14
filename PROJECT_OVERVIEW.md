# Project Overview: Agentic Hacker News Analyzer

## Purpose

This project is a specialized AI agent that periodically analyzes top stories from Hacker News and recommends the most technically relevant ones based on a fixed interest profile. Unlike generic summarizers, it focuses specifically on technical depth—programming, security research, CVE writeups, and engineering discoveries—filtering out popular but shallow content.

The agent runs autonomously via a scheduled task and maintains a conversation log for each execution, capturing the full reasoning trace including tool calls and their results.

## Architecture Evolution

Originally, the agent used a custom protocol where the LLM would emit pseudo-tool calls as text tags like `-%%curl_link%%-{https://example.com}`. The Java backend would parse these tags, execute the corresponding static method, and inject the result back as a fake user message (`+%%curl_link%%+\nresult`). This manual loop was fragile and verbose.

We've migrated to LangChain4j's native tool calling:
- Methods annotated with `@Tool` are automatically registered with the AI service
- The LLM calls them directly through structured tool-call messages
- Results are automatically inserted as tool-result messages
- The AI service manages the conversation and tool-roundtrip orchestration internally

This change simplifies the codebase significantly and aligns it with standard LLM agent patterns.

## Components

### Core Loop (`LLMService`)
Drives the agent's execution cycle:
1. Initializes conversation memory and loads system prompt
2. Obtains initial task prompt from the active `AgentTask`
3. Builds an `AiServices`-based assistant with:
   - Configured chat model (OpenAI-compatible endpoint)
   - Per-execution memory window
   - System prompt provider
   - Registered tool instances from the task
4. Invokes the assistant with the initial prompt
5. Logs the entire interaction trace:
   - System prompt (once)
   - Initial user prompt
   - Each tool call and its result (under `Actor.TOOL`)
   - Final assistant answer (under `Actor.ASSISTANT`)

### Task System (`AgentTask` interface)
Encapsulates a self-contained AI-driven workflow:
- `initialPrompt()`: Provides the kickoff message that frames the task for the LLM
- `tools()`: Returns a list of tool instances (objects with `@Tool`-annotated methods) the task needs

Currently, only one task is implemented: `HackerNewsTask`.

#### Hacker News Task (`HackerNewsTask`)
Performs the core recommendation job:
1. Fetches the current top 20 story IDs from Hacker News API
2. Fetches full item details (title, URL, etc.) for each story
3. Combines the system prompt, skill instructions, and the story list into one initial prompt
4. Registers the `CurlLinkTool` instance for fetching article content

The skill instructions (`prompts/skill_hackernews.md`) guide the LLM to:
- Evaluate each story's title for technical relevance
- Call `curl_link` on promising URLs to inspect the full content
- Ignore inaccessible pages (`PASS` results)
- Finally, return a curated list of relevant articles in a simple format

### Tools

#### Web Fetcher (`CurlLinkTool`)
A single `@Tool` method `curl_link(url)` that:
- Fetches a URL with appropriate timeouts and headers
- Parses the HTML body to extract visible text
- Returns the text, or `"PASS"` if the fetch fails

The method handles all exceptions internally to maintain the agent loop—failures become `PASS` results that the LLM can interpret as "inaccessible".

### Storage

#### Conversation Logs (`ConversationStore`)
Each agent execution gets a unique log file (`conv_YYYY-MM-DDTHH:MM:SS`) that captures:
- Timestamped messages from `SYSTEM`, `USER` (initial prompt), `TOOL`, and `ASSISTANT`
- Full tool-call arguments and results (not just final answers)
- Complete execution trace for debugging and auditing

Log files are stored in `/conv` (mapped via Docker volume).

### Scheduling

#### Cron Job (`Cron`)
Triggers the `HackerNewsTask` once per minute via `LLMService.executeAgentLoop`. Uses an atomic guard to prevent overlapping executions.

## Deployment

### Docker Setup
The project includes a multi-stage Dockerfile:
1. Maven builder stage compiles the JAR
2. Runtime stage runs the JAR on Eclipse Temurin 21 JRE
3. Mounts `/conv` and `/prompts` as volumes to persist logs and allow prompt updates without rebuilds
4. Exposes port 9011 (though the REST endpoint is currently a stub)

### Configuration (`application.properties`)
Key settings:
- `app.ai.model`: Model identifier (passed to OpenAI-compatible endpoint)
- `app.ai.secret`: API key for the LLM endpoint
- `app.ai.url`: Base URL of the OpenAI-compatible service
- `app.conversations.path`: Host path where conversation logs are written (mounted as `/conv` in Docker)

## Development Notes

### Dependencies
- LangChain4j 1.23.0 (core + OpenAI integration)
- Spring Boot 4.1.2 (web, scheduling)
- JSoup 1.24.1 (HTML parsing)
- Lombok (code generation)

### Build & Run
Standard Maven lifecycle:
```bash
./mvnw clean compile
./mvnw package
java -jar target/agent-1.0-SNAPSHOT.jar
```

Or via Docker:
```bash
docker-compose up --build
```
