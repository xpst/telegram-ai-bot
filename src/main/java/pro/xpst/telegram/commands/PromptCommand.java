package pro.xpst.telegram.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import pro.xpst.telegram.OpenAiTelegramBot;

public class PromptCommand extends BotCommand implements IBotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(PromptCommand.class);

    private static final String COMMAND = "prompt";

    private final OpenAiTelegramBot openAiTelegramBot;

    public PromptCommand(OpenAiTelegramBot openAiTelegramBot) {
        super(COMMAND, "Prints the current system prompt or sets a new one");
        this.openAiTelegramBot = openAiTelegramBot;
    }

    @Override
    public String getCommandIdentifier() {
        return getCommand();
    }

    @Override
    public void processMessage(TelegramClient aTelegramClient, Message aMessage, String[] anArguments) {
        LOGGER.debug("processMessage()");
        if (0 == anArguments.length) {
            openAiTelegramBot.sendMessage(aMessage.getChatId(),
                    "The current system prompt: " + openAiTelegramBot.getOpenAiService().getPrompt());
        } else {
            String newPrompt = aMessage.getText().substring(("/" + COMMAND).length());
            openAiTelegramBot.getOpenAiService().setSystemPrompt(newPrompt);
            openAiTelegramBot.sendMessage(aMessage.getChatId(), "Done.");
        }
    }
}
