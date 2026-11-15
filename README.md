# AgenticBot

This is my personal AI assistant system that helps me automate tasks and manage information. It's designed around autonomous agents that can perform various functions like fetching news (currently), and recommend articles to me.

## What it does

I use this system to:
- Get personalized Hacker News recommendations based on my interests.
- Schedule and automate tasks
- Send notifications via Telegram

## Structure

  - `ai/` - AI assistant logic and LLM integration
  - `tasks/` - Task implementations (like Hacker News article recommendation)
  - `notification/` - Notification services (Telegram integration)
  - `sched/` - Scheduling functionality
  - `store/` - Data storage and conversation management
- `prompts/` - System prompts that define the AI's behavior and skills, currently they contain my own skills.
- `Main.java` - Entry point of the application

The system uses a modular approach where different components handle specific responsibilities, making it easy to extend with new capabilities.