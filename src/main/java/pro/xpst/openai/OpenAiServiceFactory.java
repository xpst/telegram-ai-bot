package pro.xpst.openai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Routes chats to providers. Knows no provider types: it works with whatever {@link ChatProvider}
 * beans exist in this build (OpenAI is absent in a -DgeminiOnly build).
 */
@Component
public class OpenAiServiceFactory implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAiServiceFactory.class);

    private final List<ChatProvider> providers;
    private final ChatProvider defaultProvider;
    private final Map<Long, OpenAiService> openAiServiceMap = new HashMap<>();

    public OpenAiServiceFactory(
            List<ChatProvider> aProviders,
            @Value("${pro.xpst.default.provider:openai}") String aDefaultProvider) {
        if (aProviders.isEmpty()) {
            throw new IllegalStateException("No AI provider is available in this build");
        }
        this.providers = aProviders;

        String requested = aDefaultProvider == null ? "" : aDefaultProvider.trim().toLowerCase();
        this.defaultProvider = aProviders.stream()
                .filter(p -> p.getName().equals(requested))
                .findFirst()
                .orElse(aProviders.getFirst());
        if (!this.defaultProvider.getName().equals(requested)) {
            LOGGER.warn("Default provider '{}' is not available in this build; using '{}'",
                    requested, this.defaultProvider.getName());
        }
        LOGGER.info("AI providers in this build: {}; default for new chats: {}",
                aProviders.stream().map(ChatProvider::getName).toList(), this.defaultProvider.getName());
    }

    public OpenAiService getInstance(Long aChatId) {
        return this.openAiServiceMap.computeIfAbsent(aChatId, (s) -> this.defaultProvider.create());
    }

    public Set<Long> getChatsIds() {
        return this.openAiServiceMap.keySet();
    }

    public Set<String> getAllModels() {
        Set<String> all = new LinkedHashSet<>();
        this.providers.forEach(p -> all.addAll(p.getModels()));
        return all;
    }

    public void setModel(Long aChatId, String aModelName) {
        LOGGER.debug("setModel(chatId={}, model={})", aChatId, aModelName);
        ChatProvider target = this.providers.stream()
                .filter(p -> p.getModels().contains(aModelName))
                .findFirst()
                .orElse(null);
        if (target == null) {
            // e.g. a stale /model keyboard offering a model of a provider not in this build
            LOGGER.warn("Model '{}' is not offered by any provider in this build; ignoring (chatId={})",
                    aModelName, aChatId);
            return;
        }

        OpenAiService current = this.openAiServiceMap.get(aChatId);
        if (current == null || !target.owns(current)) {
            if (current != null) {
                LOGGER.debug("Switching chat {} to provider {}; memory will reset", aChatId, target.getName());
            }
            current = target.create();
            this.openAiServiceMap.put(aChatId, current);
        }

        current.setModel(aModelName);
    }
}
