package pro.xpst.openai;

import java.util.Set;

public interface OpenAiService {

    String generate(String aMessage);

    void reset();

    String getPrompt();

    void setSystemPrompt(String aSystemPrompt);

    void translate(String aLanguage);

    String getModel();

    void seModel(String aModel);

    Set<String> getModels();

}
