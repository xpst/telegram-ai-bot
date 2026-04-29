package pro.xpst.openai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pro.xpst.gemini.impl.GeminiServiceImpl;
import pro.xpst.openai.impl.OpenAiServiceImpl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Component
public class OpenAiServiceFactory implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAiServiceFactory.class);

    private static final String PROVIDER_GEMINI = "gemini";

    private final OpenAiChatModel openAiChatModel;
    private final GoogleGenAiChatModel geminiChatModel;
    private final String defaultOpenAiModel;
    private final String defaultGeminiModel;
    private final Set<String> openAiModels;
    private final Set<String> geminiModels;
    private final int expirationMinutes;
    private final String defaultProvider;
    private final ApplicationContext applicationContext;
    private final Map<Long, OpenAiService> openAiServiceMap = new HashMap<>();

    public OpenAiServiceFactory(
            OpenAiChatModel anOpenAiChatModel,
            GoogleGenAiChatModel aGeminiChatModel,
            @Value("${spring.ai.openai.chat.options.model}") String aDefaultOpenAiModel,
            @Value("${spring.ai.google.genai.chat.options.model}") String aDefaultGeminiModel,
            @Value("${pro.xpst.openai.models}") Set<String> anOpenAiModels,
            @Value("${pro.xpst.gemini.models}") Set<String> aGeminiModels,
            @Value("${pro.xpst.openai.conversation.expiration:60}") int expirationMinutes,
            @Value("${pro.xpst.default.provider:openai}") String aDefaultProvider,
            ApplicationContext applicationContext) {
        this.openAiChatModel = anOpenAiChatModel;
        this.geminiChatModel = aGeminiChatModel;
        this.defaultOpenAiModel = aDefaultOpenAiModel;
        this.defaultGeminiModel = aDefaultGeminiModel;
        this.openAiModels = anOpenAiModels;
        this.geminiModels = aGeminiModels;
        this.expirationMinutes = expirationMinutes;
        this.defaultProvider = aDefaultProvider == null ? "" : aDefaultProvider.trim().toLowerCase();
        this.applicationContext = applicationContext;
    }

    public OpenAiService getInstance(Long aChatId) {
        this.openAiServiceMap.computeIfAbsent(aChatId,
                (s) -> PROVIDER_GEMINI.equals(this.defaultProvider) ? createGemini() : createOpenAi());
        return this.openAiServiceMap.get(aChatId);
    }

    public Set<Long> getChatsIds() {
        return this.openAiServiceMap.keySet();
    }

    public Set<String> getAllModels() {
        Set<String> all = new LinkedHashSet<>(this.openAiModels);
        all.addAll(this.geminiModels);
        return all;
    }

    public void setModel(Long aChatId, String aModelName) {
        LOGGER.debug("setModel(chatId={}, model={})", aChatId, aModelName);
        boolean isGemini = this.geminiModels.contains(aModelName);
        OpenAiService current = this.openAiServiceMap.get(aChatId);

        if (current == null) {
            current = isGemini ? createGemini() : createOpenAi();
            this.openAiServiceMap.put(aChatId, current);
        } else if (isGemini && !(current instanceof GeminiServiceImpl)) {
            LOGGER.debug("Switching chat {} from OpenAI to Gemini; memory will reset", aChatId);
            current = createGemini();
            this.openAiServiceMap.put(aChatId, current);
        } else if (!isGemini && !(current instanceof OpenAiServiceImpl)) {
            LOGGER.debug("Switching chat {} from Gemini to OpenAI; memory will reset", aChatId);
            current = createOpenAi();
            this.openAiServiceMap.put(aChatId, current);
        }

        current.setModel(aModelName);
    }

    private OpenAiService createOpenAi() {
        return this.applicationContext.getBean(
                OpenAiServiceImpl.class,
                this.openAiChatModel,
                this.defaultOpenAiModel,
                this.expirationMinutes);
    }

    private OpenAiService createGemini() {
        return this.applicationContext.getBean(
                GeminiServiceImpl.class,
                this.geminiChatModel,
                this.defaultGeminiModel,
                this.expirationMinutes);
    }
}
