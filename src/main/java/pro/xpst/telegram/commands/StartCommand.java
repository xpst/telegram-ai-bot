package pro.xpst.telegram.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import pro.xpst.telegram.OpenAiTelegramBot;

public class StartCommand extends BotCommand implements IBotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartCommand.class);

    private final OpenAiTelegramBot openAiTelegramBot;

    public StartCommand(OpenAiTelegramBot openAiTelegramBot) {
        super("start", "Prints the list of commands");
        this.openAiTelegramBot = openAiTelegramBot;
    }

    @Override
    public String getCommandIdentifier() {
        return getCommand();
    }

    @Override
    public void processMessage(TelegramClient aTelegramClient, Message aMessage, String[] anArguments) {
        LOGGER.debug("processMessage()");
        openAiTelegramBot.sendMessage(aMessage.getChatId(), aMessage.getFrom().getUserName() + ", available commands:\n" +
                        """
                     /start - prints the list of commands
                     /reset - removes the current conversation history and sets the system prompt to the default one
                     /model <model_name> - prints the current model and allows to set a new one
                     /prompt <model_name> - prints the current system prompt or sets a new one if specified
                     /translate <language> - translates to a specified language
                    """
                );
    }
}
