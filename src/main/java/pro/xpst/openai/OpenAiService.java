package pro.xpst.openai;

import java.time.Duration;
import java.util.Set;

public interface OpenAiService {

    String generate(String aMessage);

    void reset();

    String getPrompt();

    void setSystemPrompt(String aSystemPrompt);

    void translate(String aLanguage);

    String getModel();

    void setModel(String aModel);

    Set<String> getModels();

    /**
     * Get the current expiration time for conversation history
     * @return Duration representing the expiration time
     */
    Duration getConversationExpirationTime();

    /**
     * Set the expiration time for conversation history
     * @param expirationTime Duration representing the expiration time
     */
    void setConversationExpirationTime(Duration expirationTime);
}
