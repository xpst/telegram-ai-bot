# JavaOpenAI

A Java-based Telegram bot that integrates with OpenAI's API to provide conversational AI capabilities. This bot allows users to interact with various OpenAI models, customize system prompts, and translate messages to different languages.

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Setup Instructions](#setup-instructions)
  - [Environment Variables](#environment-variables)
  - [Configuration Options](#configuration-options)
  - [Running Locally](#running-locally)
  - [Docker Deployment](#docker-deployment)
- [Bot Commands](#bot-commands)
  - [Basic Commands](#basic-commands)
  - [Advanced Commands](#advanced-commands)
  - [Administrative Commands](#administrative-commands)
- [Common Use Cases](#common-use-cases)
- [Project Structure](#project-structure)
- [Dependencies](#dependencies)
- [Future Features](#future-features)


## Features

- Interact with OpenAI's and Google Gemini language models through Telegram
- Support for multiple OpenAI models (gpt-4o, gpt-5-nano, etc.) and Google Gemini models (gemini-3.1-flash-lite-preview, etc.)
- Per-chat provider switching via `/model` (note: switching between OpenAI and Gemini resets that chat's memory)
- Customizable system prompts
- Conversation history management
- Translation capabilities
- User access control
- Docker support for easy deployment

## Prerequisites

- Java 17 or higher
- Maven
- OpenAI API key
- Google AI Studio API key (for Gemini models)
- Telegram Bot token (obtained from BotFather)
- Docker (optional, for containerized deployment)

## Setup Instructions

### Environment Variables

The following environment variables need to be set:

- `OPENAI_API_KEY`: Your OpenAI API key
- `GEMINI_API_KEY`: Your Google AI Studio API key (for Gemini models)
- `BOT_NAME`: Your Telegram bot username
- `BOT_TOKEN`: Your Telegram bot token

### Configuration Options

The application can be configured through the `application.properties` file:

```properties
# Server configuration
server.port=8191
spring.application.name=JavaOpenAI

# OpenAI configuration
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-5-nano

# Google Gemini configuration
spring.ai.google.genai.api-key=${GEMINI_API_KEY}
spring.ai.google.genai.chat.options.model=gemini-3.1-flash-lite-preview

# Available models for selection (per provider)
pro.xpst.openai.models=gpt-5,gpt-4o-mini,gpt-5-mini,gpt-5-nano,o4-mini
pro.xpst.gemini.models=gemini-3.1-flash-lite-preview

# Default provider for brand-new chats: openai or gemini
pro.xpst.default.provider=gemini

# Telegram bot configuration
pro.xpst.telegram.bot.username=${BOT_NAME}
pro.xpst.telegram.bot.token=${BOT_TOKEN}

# User access control (comma-separated list of Telegram user IDs)
pro.xpst.telegram.bot.users.allowed=123456789,987654321
```

#### Configuration Options Explained

| Option | Description | Default |
|--------|-------------|---------|
| `server.port` | The port on which the application runs | 8191 |
| `spring.ai.openai.api-key` | Your OpenAI API key | - |
| `spring.ai.openai.chat.options.model` | The default OpenAI model to use | gpt-5-nano |
| `spring.ai.google.genai.api-key` | Your Google AI Studio API key | - |
| `spring.ai.google.genai.chat.options.model` | The default Gemini model to use | gemini-3.1-flash-lite-preview |
| `pro.xpst.openai.models` | List of available OpenAI models for selection | gpt-5,gpt-4o-mini,gpt-5-mini,gpt-5-nano,o4-mini |
| `pro.xpst.gemini.models` | List of available Gemini models for selection | gemini-3.1-flash-lite-preview |
| `pro.xpst.default.provider` | Provider used for the first message in a new chat (`openai` or `gemini`) | openai |
| `pro.xpst.telegram.bot.username` | Your Telegram bot username | - |
| `pro.xpst.telegram.bot.token` | Your Telegram bot token | - |
| `pro.xpst.telegram.bot.users.allowed` | List of allowed Telegram user IDs | - |

### Running Locally

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/JavaOpenAI.git
   cd JavaOpenAI
   ```

2. Set up environment variables or update the `application.properties` file with your API keys and tokens.

3. Build the project:
   ```bash
   mvn clean package
   ```

4. Run the application:
   ```bash
   java -jar target/java-openai-0.0.1.jar
   ```

### Docker Deployment

1. Make sure Docker is installed on your system.

2. Build the project:
   ```bash
   mvn clean package
   ```

3. Create or update the `application.properties` file in `src/main/resources` with your configuration.

4. Run the Docker container:
   ```bash
   docker-compose up -d
   ```

## Bot Commands

### Basic Commands

- `/start` - Displays a list of all available commands
- `/reset` - Removes the current conversation history and resets the system prompt to the default one

### Advanced Commands

- `/model [model name]` - Without arguments, displays the current model. With a model name argument, changes the model to the specified one. You can also select a model from the inline keyboard that appears, which lists both OpenAI and Gemini models.

  Note: switching between an OpenAI model and a Gemini model (or vice versa) resets that chat's conversation memory; switching between two models of the same provider preserves it.

  Example: `/model gpt-4o`

  Example: `/model gemini-3.1-flash-lite-preview`

- `/prompt [new system prompt]` - Without arguments, displays the current system prompt. With an argument, sets a new system prompt.

  Example: `/prompt You are a helpful assistant that speaks like Shakespeare.`

- `/translate [language]` - Sets up translation to the specified language for subsequent messages.

  Example: `/translate Spanish`

### Administrative Commands

- `/admin` - (Admin only) Displays a list of all active chat IDs

## Common Use Cases

### General Conversation

Simply send a message to the bot, and it will respond using the configured OpenAI model.

Example:
```
User: What is the capital of France?
Bot: The capital of France is Paris.
```

### Changing the AI Model

If you want to switch to a more capable model:

```
User: /model gpt-4o
Bot: Done, current model is: gpt-4o
```

Or switch provider to Google Gemini:

```
User: /model gemini-3.1-flash-lite-preview
Bot: Done, current model is: gemini-3.1-flash-lite-preview
```

### Customizing the System Prompt

To make the AI respond in a specific way:

```
User: /prompt You are a helpful assistant that specializes in explaining complex topics in simple terms.
Bot: Done.

User: Explain quantum computing
Bot: [Simplified explanation of quantum computing]
```

### Translation

To get responses in a different language:

```
User: /translate Spanish
Bot: Done.

User: Tell me about the history of jazz music
Bot: [Response in Spanish about the history of jazz music]
```

### Resetting the Conversation

When you want to start a fresh conversation:

```
User: /reset
Bot: Done.
```

## Project Structure

The project is built using Spring Boot and is organized as follows:

- `pro.xpst.openai` - OpenAI service integration and the shared `OpenAiService` interface; the `OpenAiServiceFactory` here also routes per-chat between providers
- `pro.xpst.gemini` - Google Gemini service implementation
- `pro.xpst.telegram` - Telegram bot implementation
- `pro.xpst.telegram.commands` - Command handlers for the Telegram bot

## Dependencies

This project is based on:

- [Spring AI](https://docs.spring.io/spring-ai/reference/index.html) ([GitHub](https://github.com/spring-projects/spring-ai))
  - [`spring-ai-starter-model-openai`](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html)
  - [`spring-ai-starter-model-google-genai`](https://docs.spring.io/spring-ai/reference/api/chat/google-genai-chat.html)
- [TelegramBots](https://github.com/rubenlagus/TelegramBots) by Ruben Bermudez

Examples and inspiration from Dan Vega:
- [spring-into-ai](https://github.com/danvega/spring-into-ai)
- [production-ai](https://github.com/danvega/production-ai)

## Future Features

The following features are planned for future releases:

- Support for image generation and processing
- Voice message support
- User preferences storage

