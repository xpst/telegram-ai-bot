package pro.xpst.openai.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.xpst.openai.OpenAiService;

import java.util.Set;

@Service
public class OpenAiServiceImpl implements OpenAiService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAiServiceImpl.class);

    private static final String DEFAULT_SYSTEM_PROMPT = "You are a helpful assistant.";

    private static final PromptTemplate LANG = new PromptTemplate("""
             You are an interpreter.
             You will be provided with a text.
             You will translate it to {value}
            """);

    private final ChatClient chatClient;

    private InMemoryChatMemory inMemoryChatMemory;

    private MessageChatMemoryAdvisor memoryAdvisor;

    private String systemPrompt;

    private OpenAiChatOptions options;

    @Value("${pro.xpst.openai.models}")
    private Set<String> models;

    public OpenAiServiceImpl(ChatClient.Builder aBuilder, @Value("${spring.ai.openai.chat.options.model}") String aModel) {
        LOGGER.debug("OpenAiServiceImpl()");

        systemPrompt = DEFAULT_SYSTEM_PROMPT;

        inMemoryChatMemory = new InMemoryChatMemory();
        memoryAdvisor = new MessageChatMemoryAdvisor(inMemoryChatMemory);
        this.chatClient = aBuilder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor())
                .build();

        options = OpenAiChatOptions.builder()
                .withModel(aModel)
                .build();
    }

    @Override
    public String generate(String aMessage) {
        LOGGER.debug("Generating a message");
        String response = chatClient
                .prompt()
                .options(options)
                .advisors(memoryAdvisor)
                .system(systemPrompt)
                .user(aMessage)
                .call()
                .content();
        LOGGER.debug("Message has been generated");
        return response;
    }

    @Override
    public void reset() {
        LOGGER.debug("reset()");
        inMemoryChatMemory = new InMemoryChatMemory();
        memoryAdvisor = new MessageChatMemoryAdvisor(inMemoryChatMemory);
        systemPrompt = DEFAULT_SYSTEM_PROMPT;
    }

    @Override
    public String getPrompt() {
        return systemPrompt;
    }

    @Override
    public void setSystemPrompt(String aSystemPrompt) {
        systemPrompt = aSystemPrompt;
    }

    @Override
    public void translate(String aLanguage) {
        LANG.add("value", aLanguage);
        systemPrompt = LANG.create().getContents();
    }

    @Override
    public String getModel() {
        return options.getModel();
    }

    @Override
    public void seModel(String aModel) {
        options = OpenAiChatOptions.builder()
                .withModel(aModel)
                .build();
    }

    @Override
    public Set<String> getModels() {
        return models;
    }

}
