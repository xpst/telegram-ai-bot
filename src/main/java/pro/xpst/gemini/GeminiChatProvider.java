package pro.xpst.gemini;

import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pro.xpst.gemini.impl.GeminiServiceImpl;
import pro.xpst.openai.ChatProvider;
import pro.xpst.openai.OpenAiService;

import java.util.Set;

@Component
@Order(2)
public class GeminiChatProvider implements ChatProvider {

    private final GoogleGenAiChatModel chatModel;
    private final String defaultModel;
    private final Set<String> models;
    private final int expirationMinutes;
    private final ApplicationContext applicationContext;

    public GeminiChatProvider(GoogleGenAiChatModel aChatModel,
                              @Value("${spring.ai.google.genai.chat.options.model}") String aDefaultModel,
                              @Value("${pro.xpst.gemini.models}") Set<String> aModels,
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
        return "gemini";
    }

    @Override
    public Set<String> getModels() {
        return this.models;
    }

    @Override
    public OpenAiService create() {
        return this.applicationContext.getBean(GeminiServiceImpl.class,
                this.chatModel, this.defaultModel, this.expirationMinutes);
    }

    @Override
    public boolean owns(OpenAiService aService) {
        return aService instanceof GeminiServiceImpl;
    }
}
