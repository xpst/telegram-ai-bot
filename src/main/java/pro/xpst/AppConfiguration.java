package pro.xpst;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pro.xpst.telegram.OpenAiTelegramBot;

import java.time.Duration;

@Configuration
public class AppConfiguration {

    @Bean
    public BotSession sessionStart(TelegramBotsLongPollingApplication aBotApplication, OpenAiTelegramBot aBot) throws TelegramApiException {
        return aBotApplication.registerBot(aBot.getBotToken(), aBot);
    }

    @Bean
    public TelegramBotsLongPollingApplication application() {
        return new TelegramBotsLongPollingApplication();
    }

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder -> restClientBuilder
                .requestFactory(
                        ClientHttpRequestFactories.get(
                            ClientHttpRequestFactorySettings.DEFAULTS
                                .withConnectTimeout(Duration.ofSeconds(60))
                                .withReadTimeout(Duration.ofSeconds(60))
                        )
                );
    }
}
