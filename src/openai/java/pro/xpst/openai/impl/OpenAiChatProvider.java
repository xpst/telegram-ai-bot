package pro.xpst.openai.impl;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.xpst.openai.ChatProvider;
import pro.xpst.openai.OpenAiService;

import java.util.Set;

/**
 * Lives in src/openai/java, which is only compiled by the "openai" Maven profile
 * (active unless the build runs with -DgeminiOnly).
 */
@Component
@Order(1)
public class OpenAiChatProvider implements ChatProvider {

    private final OpenAiChatModel chatModel;
    private final String defaultModel;
    private final Set<String> models;
    private final int expirationMinutes;
    private final ApplicationContext applicationContext;

    public OpenAiChatProvider(OpenAiChatModel aChatModel,
                              @Value("${spring.ai.openai.chat.options.model}") String aDefaultModel,
                              @Value("${pro.xpst.openai.models}") Set<String> aModels,
                              @Value("${pro.xpst.openai.conversation.expiration:60}") int anExpirationMinutes,
                              ApplicationContext anApplicationContext) {
        this.chatModel = aChatModel;
        this.defaultModel = aDefaultModel;
        this.models = aModels;
        this.expirationMinutes = anExpirationMinutes;
        this.applicationContext = anApplicationContext;
    }

    @Override
    public String getName() {
        return "openai";
    }

    @Override
    public Set<String> getModels() {
        return this.models;
    }

    @Override
    public OpenAiService create() {
        return this.applicationContext.getBean(OpenAiServiceImpl.class,
                this.chatModel, this.defaultModel, this.expirationMinutes);
    }

    @Override
    public boolean owns(OpenAiService aService) {
        return aService instanceof OpenAiServiceImpl;
    }
}
