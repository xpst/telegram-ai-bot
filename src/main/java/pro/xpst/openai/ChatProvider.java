package pro.xpst.openai;

import java.util.Set;

/**
 * One AI provider (OpenAI, Gemini, ...) that can create per-chat services.
 * Implementations are Spring beans; {@link OpenAiServiceFactory} picks them up as a list,
 * so a provider whose code is not compiled into the build (e.g. OpenAI in a -DgeminiOnly build)
 * simply does not exist at runtime.
 */
public interface ChatProvider {

    /**
     * @return provider id as used in {@code pro.xpst.default.provider}, e.g. "openai" or "gemini"
     */
    String getName();

    /**
     * @return models this provider offers in the /model keyboard
     */
    Set<String> getModels();

    /**
     * @return a fresh per-chat service using the provider's default model
     */
    OpenAiService create();

    /**
     * @return true if the given service was created by this provider
     */
    boolean owns(OpenAiService aService);
}
