package pro.xpst.telegram.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import pro.xpst.telegram.OpenAiTelegramBot;

public class TranslateCommand extends BotCommand implements IBotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranslateCommand.class);

    private final OpenAiTelegramBot openAiTelegramBot;

    public TranslateCommand(OpenAiTelegramBot openAiTelegramBot) {
        super("translate", "Translates to a specified language");
        this.openAiTelegramBot = openAiTelegramBot;
    }

    @Override
    public String getCommandIdentifier() {
        return getCommand();
    }

    @Override
    public void processMessage(TelegramClient aTelegramClient, Message aMessage, String[] anArguments) {
        LOGGER.debug("processMessage()");
        if (anArguments.length != 1) {
            openAiTelegramBot.sendMessage(aMessage.getChatId(), aMessage.getFrom().getUserName() + ", please specify a language");
        } else {
            openAiTelegramBot.getOpenAiService().translate(anArguments[0]);
            openAiTelegramBot.sendMessage(aMessage.getChatId(), "Done.");
        }
    }
}
