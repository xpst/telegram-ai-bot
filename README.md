# JavaOpenAI

OpenAI API Telegram bot on Java

Based on:

[Spring AI](https://docs.spring.io/spring-ai/reference/index.html), [Spring AI repo](https://github.com/spring-projects/spring-ai)

[TelegramBots from Ruben Bermudez](https://github.com/rubenlagus/TelegramBots)

Examples from Dan Vega:

https://github.com/danvega/spring-into-ai

https://github.com/danvega/production-ai

## Settings


Open AI API Key
```
spring.ai.openai.api-key=${OPENAI_API_KEY}
```

Telegram bot name and token for the bot
```
pro.xpst.telegram.bot.username=${BOT_NAME}
pro.xpst.telegram.bot.token=${BOT_TOKEN}
```

## Bot commands

```
    /start - prints the list of commands
    /reset - removes the current conversation history and sets the system prompt to the default one
    /model <model name> - prints the current model and allows to set a new one (gpt-3.5-turbo or gpt-4o)
    /prompt <new system prompt> - prints the current system prompt or sets a new one if specified
    /translate <language> - translates to a specified language
```
