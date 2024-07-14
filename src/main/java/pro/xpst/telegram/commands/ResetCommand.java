package pro.xpst.telegram.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import pro.xpst.telegram.OpenAiTelegramBot;

public class ResetCommand extends BotCommand implements IBotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResetCommand.class);

    private final OpenAiTelegramBot openAiTelegramBot;

    public ResetCommand(OpenAiTelegramBot openAiTelegramBot) {
        super("reset", "Removes the current conversation history");
        this.openAiTelegramBot = openAiTelegramBot;
    }

    @Override
    public String getCommandIdentifier() {
        return getCommand();
    }

    @Override
    public void processMessage(TelegramClient aTelegramClient, Message aMessage, String[] anArguments) {
        LOGGER.debug("processMessage()");
        openAiTelegramBot.getOpenAiService().reset();
        openAiTelegramBot.sendMessage(aMessage.getChatId(), "Done.");
    }
}
