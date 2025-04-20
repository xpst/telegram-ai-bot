package pro.xpst.telegram;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.extensions.bots.commandbot.CommandLongPollingTelegramBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pro.xpst.openai.OpenAiService;
import pro.xpst.openai.OpenAiServiceFactory;
import pro.xpst.telegram.commands.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component("openAiTelegramBot")
public class OpenAiTelegramBot extends CommandLongPollingTelegramBot implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAiTelegramBot.class);

    @Value("${pro.xpst.telegram.bot.users.allowed}")
    private Set<Long> allowedUsers;

    @Getter
    private final String botToken;
    private final OpenAiServiceFactory openAiServiceFactory;
    private final ModelCommand modelCommand;

    public OpenAiTelegramBot(@Value("${pro.xpst.telegram.bot.token}") String aBotToken, @Value("${pro.xpst.telegram.bot.username}") String aBotName, OpenAiServiceFactory openAiServiceFactory) {
        super(new OkHttpTelegramClient(aBotToken), true, () -> aBotName);
        this.botToken = aBotToken;

        this.modelCommand = new ModelCommand(this);

        this.registerAll(
                new ResetCommand(this),
                new StartCommand(this),
                new PromptCommand(this),
                new TranslateCommand(this),
                this.modelCommand);
        this.setMenuButton();
        this.register(new AdminCommand(this));
        this.openAiServiceFactory = openAiServiceFactory;
    }

    public OpenAiService getOpenAiService(Long aChatId) {
        return this.openAiServiceFactory.getInstance(aChatId);
    }

    public Set<Long> getChatsIds() {
        return this.openAiServiceFactory.getChatsIds();
    }

    @Override
    public void processNonCommandUpdate(Update anUpdate) {
        LOGGER.debug("processNonCommandUpdate()");

        if (null == anUpdate) {
            return;
        }

        if (anUpdate.hasCallbackQuery() && ModelCommand.isModelCallbackQuery(anUpdate.getCallbackQuery())) {
            modelCommand.processCallbackQuery(anUpdate);
        }

        if (anUpdate.hasMessage()) {
            processMessage(anUpdate);
        }
    }

    private void processMessage(Update anUpdate) {
        LOGGER.debug("processMessage()");

        if (!anUpdate.getMessage().hasText()
                || null == anUpdate.getMessage().getChat()) {
            return;
        }

        Message message = anUpdate.getMessage();

        if (!isUserAllowed(message)) {
            return;
        }

        LOGGER.debug("We've got a message: MessageId: {}, UserId: {}, ChatId: {}",
                message.getMessageId(), message.getFrom().getId(), message.getChatId());
        this.sendChatAction(message.getChatId().toString(), ActionType.TYPING.toString());
        if (message.hasText()) {
            this.sendMessage(anUpdate.getMessage().getChatId(), this.getOpenAiService(message.getChatId()).generate(anUpdate.getMessage().getText()));
        }
    }

    @Override
    public boolean filter(Message aMessage) {
        if (!this.isUserAllowed(aMessage)) {
            return false;
        } else {
            LOGGER.debug("Got message: {} from User: {} ({})", new Object[]{aMessage.getText(), aMessage.getFrom().getUserName(), aMessage.getFrom().getId()});
            return super.filter(aMessage);
        }
    }

    private boolean isUserAllowed(Message aMessage) {
        Long userId = aMessage.getFrom().getId();
        return this.allowedUsers == null || this.allowedUsers.isEmpty() || this.allowedUsers.contains(userId);
    }

    private void setMenuButton() {
        LOGGER.debug("setMenuButton()");
        // Create your menu here and set it
        List<BotCommand> commands = new ArrayList<>();
        this.getRegisteredCommands().forEach(iBotCommand -> commands.add((BotCommand) iBotCommand));
        try {
            this.telegramClient.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException ex) {
            LOGGER.error("Error adding menu commands", ex);
        }
    }

    public void sendInlineKeyboardMarkup(Long chatId, String message, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), message);

        // Set the markup to the message
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        try {
            this.telegramClient.execute(sendMessage);
        } catch (TelegramApiException ex) {
            LOGGER.error("Error while sending an InlineKeyboardMarkup", ex);

        }
    }

    public void sendMessage(Long aChatId, String aMessage) {
        LOGGER.debug("sendMessage()");
        if (aMessage.length() > 4096) {
            List<String> messageParts = new ArrayList<>();

            int endIndex;
            for (int startIndex = 0; startIndex < aMessage.length(); startIndex = endIndex) {
                endIndex = Math.min(startIndex + 4096, aMessage.length());
                messageParts.add(aMessage.substring(startIndex, endIndex));
            }

            for (String part : messageParts) {
                this.sendMessage(aChatId, part);
            }
        } else {
            SendMessage snd = new SendMessage(aChatId.toString(), aMessage);
            snd.enableMarkdown(true);
            snd.setParseMode(ParseMode.MARKDOWN);

            try {
                this.telegramClient.execute(snd);
            } catch (Exception ex) {
                LOGGER.error("Error while sending a message: {}", aMessage, ex);
            }
        }

    }

    public void deleteMessage(Long aChatId, Integer aMessageId) {
        LOGGER.debug("deleteMessage()");
        DeleteMessage deleteMessage = new DeleteMessage(aChatId.toString(), aMessageId);
        try {
            this.telegramClient.execute(deleteMessage);
        } catch (Exception ex) {
            LOGGER.error("Error while deleting a message", ex);
        }
    }

    private void sendChatAction(String aChatId, String aChatAction) {
        LOGGER.debug("sendChatAction()");
        SendChatAction sendChatAction = new SendChatAction(aChatId, aChatAction);
        try {
            this.telegramClient.execute(sendChatAction);
        } catch (TelegramApiException ex) {
            LOGGER.error("Error sending chat action", ex);
        }
    }

}
