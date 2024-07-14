package pro.xpst.telegram.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import pro.xpst.telegram.OpenAiTelegramBot;

import java.util.List;
import java.util.stream.Collectors;

public class ModelCommand extends BotCommand implements IBotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelCommand.class);

    private static final String COMMAND = "model";
    private static final String DELIMITER = ":";

    private final OpenAiTelegramBot openAiTelegramBot;

    public ModelCommand(OpenAiTelegramBot openAiTelegramBot) {
        super(COMMAND, "Prints the current model and allows to set a new one");
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
                    "The current model is: " + openAiTelegramBot.getOpenAiService().getModel());
            openAiTelegramBot.sendInlineKeyboardMarkup(aMessage.getChatId(), "Please specify a model", createButtons());
        } else if (anArguments.length > 1) {
            openAiTelegramBot.sendMessage(aMessage.getChatId(), aMessage.getFrom().getUserName() + ", please specify a model");
        } else {
            changeModel(aMessage.getChatId(), anArguments[0]);
        }
    }

    public static boolean isModelCallbackQuery(CallbackQuery aCallbackquery) {
        LOGGER.debug("isModelCallbackQuery()");
        return null != aCallbackquery.getData() && aCallbackquery.getData().startsWith(COMMAND);
    }

    private void changeModel(Long aChatId, String aModel) {
        LOGGER.debug("changeModel()");
        openAiTelegramBot.getOpenAiService().seModel(aModel);
        openAiTelegramBot.sendMessage(aChatId,
                "Done, current model is: " + openAiTelegramBot.getOpenAiService().getModel());
    }

    private InlineKeyboardMarkup createButtons() {
        LOGGER.debug("createButtons()");

        List<InlineKeyboardButton> buttons = openAiTelegramBot.getOpenAiService().getModels()
                .stream()
                .map(s ->
                        InlineKeyboardButton
                                .builder()
                                .text(s)
                                .callbackData(COMMAND + DELIMITER + s)
                                .build())
                .collect(Collectors.toList());

        // Set the keyboard to the markup
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(new InlineKeyboardRow(buttons))
                .build();
    }

    public void processCallbackQuery(Update anUpdate) {
        LOGGER.debug("processCallbackQuery()");

        CallbackQuery callbackquery = anUpdate.getCallbackQuery();
        if (null != callbackquery.getData() && callbackquery.getData().startsWith(COMMAND)) {
            String[] data = callbackquery.getData().split(DELIMITER);
            changeModel(callbackquery.getMessage().getChatId(), data[1]);

            openAiTelegramBot.deleteMessage(callbackquery.getMessage().getChatId(), callbackquery.getMessage().getMessageId());
        }
    }

}
