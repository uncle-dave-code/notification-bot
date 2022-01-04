package com.uncledavecode.notificator.bot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class BotStarter implements CommandLineRunner {

    private final NotificatorBot notificatorBot;

    public BotStarter(NotificatorBot notificatorBot) {
        this.notificatorBot = notificatorBot;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(this.notificatorBot);
        } catch (

        TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
