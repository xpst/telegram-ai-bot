package pro.xpst.openai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pro.xpst.openai.impl.OpenAiServiceImpl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class OpenAiServiceFactory implements Serializable {
    private final ChatClient.Builder builder;
    private final String defaultModel;
    private final int expirationMinutes;
    private final ApplicationContext applicationContext;
    private final Map<Long, OpenAiService> openAiServiceMap = new HashMap<>();

    public OpenAiServiceFactory(
            ChatClient.Builder aBuilder,
            @Value("${spring.ai.openai.chat.options.model}") String aModel,
            @Value("${pro.xpst.openai.conversation.expiration:60}") int expirationMinutes,
            ApplicationContext applicationContext) {
        this.builder = aBuilder;
        this.defaultModel = aModel;
        this.expirationMinutes = expirationMinutes;
        this.applicationContext = applicationContext;
    }

    public OpenAiService getInstance(Long aChatId) {
        this.openAiServiceMap.computeIfAbsent(
                aChatId,
                (s) -> this.applicationContext.getBean(
                        OpenAiServiceImpl.class,
                        this.builder,
                        this.defaultModel,
                        this.expirationMinutes));
        return this.openAiServiceMap.get(aChatId);
    }

    public Set<Long> getChatsIds() {
        return this.openAiServiceMap.keySet();
    }
}
