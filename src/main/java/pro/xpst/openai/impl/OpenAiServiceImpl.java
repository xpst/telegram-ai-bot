package pro.xpst.openai.impl;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pro.xpst.openai.OpenAiService;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Component
@Scope("prototype")
public class OpenAiServiceImpl implements OpenAiService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAiServiceImpl.class);
    private static final String DEFAULT_SYSTEM_PROMPT = "You are a helpful assistant.";
    private static final PromptTemplate LANG = new PromptTemplate(" You are an interpreter.\n You will be provided with a text.\n You will translate it to {value}\n");
    private static final Duration DEFAULT_EXPIRATION_TIME = Duration.ofHours(1); // Fallback default if property is not set

    private final ChatClient chatClient;
    private InMemoryChatMemory inMemoryChatMemory;
    private MessageChatMemoryAdvisor memoryAdvisor;
    @Setter
    private String systemPrompt;
    private OpenAiChatOptions options;
    @Getter
    @Value("${pro.xpst.openai.models}")
    private Set<String> models;

    private Duration conversationExpirationTime;
    private Instant lastInteractionTime;

    public OpenAiServiceImpl(ChatClient.Builder aBuilder,
                        @Value("${spring.ai.openai.chat.options.model}") String aModel,
                        @Value("${pro.xpst.openai.conversation.expiration:60}") int expirationMinutes) {
        LOGGER.debug("OpenAiServiceImpl()");
        this.systemPrompt = DEFAULT_SYSTEM_PROMPT;
        this.inMemoryChatMemory = new InMemoryChatMemory();
        this.memoryAdvisor = new MessageChatMemoryAdvisor(this.inMemoryChatMemory);
        this.chatClient = aBuilder.defaultSystem(this.systemPrompt).defaultAdvisors(new CallAroundAdvisor[]{new SimpleLoggerAdvisor()}).build();
        this.options = OpenAiChatOptions.builder().model(aModel).build();

        // Set conversation expiration time from properties or use default
        this.conversationExpirationTime = expirationMinutes > 0
            ? Duration.ofMinutes(expirationMinutes)
            : DEFAULT_EXPIRATION_TIME;
        LOGGER.debug("Setting conversation expiration time to: {} minutes", this.conversationExpirationTime.toMinutes());

        this.lastInteractionTime = Instant.now();
    }

    @Override
    public Duration getConversationExpirationTime() {
        return this.conversationExpirationTime;
    }

    @Override
    public void setConversationExpirationTime(Duration expirationTime) {
        LOGGER.debug("Setting conversation expiration time to: {}", expirationTime);
        this.conversationExpirationTime = expirationTime;
    }

    public String generate(String aMessage) {
        LOGGER.debug("Generating a message");

        // Check if conversation has expired
        Instant now = Instant.now();
        if (lastInteractionTime != null &&
            Duration.between(lastInteractionTime, now).compareTo(conversationExpirationTime) > 0) {
            LOGGER.debug("Conversation has expired. Resetting memory.");
            reset();
        }

        String response = this.chatClient.prompt().options(this.options).advisors(new CallAroundAdvisor[]{this.memoryAdvisor}).system(this.systemPrompt).user(aMessage).call().content();

        // Update last interaction time
        this.lastInteractionTime = Instant.now();

        LOGGER.debug("Message has been generated");
        return response;
    }

    public void reset() {
        LOGGER.debug("reset()");
        this.inMemoryChatMemory = new InMemoryChatMemory();
        this.memoryAdvisor = new MessageChatMemoryAdvisor(this.inMemoryChatMemory);
        this.systemPrompt = DEFAULT_SYSTEM_PROMPT;
        this.lastInteractionTime = Instant.now();
    }

    public String getPrompt() {
        return this.systemPrompt;
    }

    public void translate(String aLanguage) {
        LANG.add("value", aLanguage);
        this.systemPrompt = LANG.create().getContents();
    }

    public String getModel() {
        return this.options.getModel();
    }

    public void setModel(String aModel) {
        this.options = OpenAiChatOptions.builder().model(aModel).build();
    }

}
