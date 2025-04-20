package pro.xpst.telegram.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import pro.xpst.telegram.OpenAiTelegramBot;

import java.util.stream.Collectors;

public class AdminCommand extends BotCommand implements IBotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminCommand.class);

    private final OpenAiTelegramBot openAiTelegramBot;

    public AdminCommand(OpenAiTelegramBot openAiTelegramBot) {
        super("admin", "Get currents chats ids");
        this.openAiTelegramBot = openAiTelegramBot;
    }

    @Override
    public String getCommandIdentifier() {
        return getCommand();
    }

    @Override
    public void processMessage(TelegramClient aTelegramClient, Message aMessage, String[] anArguments) {
        LOGGER.debug("processMessage()");
        openAiTelegramBot.sendMessage(aMessage.getChatId(),
                "Chats ids: " + openAiTelegramBot.getChatsIds()
                        .stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", ")));
    }
}
