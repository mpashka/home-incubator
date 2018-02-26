package com.github.mpashka.spy.runner;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class TelegramBot extends TelegramLongPollingBot {

    static {
        ApiContextInitializer.init();
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("Update. " + update);
        if (update.hasMessage()) {
            System.out.println("Update received. ChatID:" + update.getMessage().getChatId());
            System.out.println("Msg:" + update.getMessage().getText());
            System.out.println("Caption:" + update.getMessage().getCaption());
            System.out.println("Contact:" + update.getMessage().getContact());
        }

/*
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
                    .setChatId(update.getMessage().getChatId())
                    .setText("Мой ответ: " + update.getMessage().getText());
            try {
                execute(message); // Call method to send the message
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
*/
    }

    @Override
    public String getBotUsername() {
        // TODO
        return "DanyaSpyBot";
    }

    @Override
    public String getBotToken() {
        // TODO
        return "542396511:AAFmVeBNasQdDmVGRHe8pS-_LhtDETu_HfM";
    }

    public void sendToChat(String messageStr) {
        SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
                .setChatId(-83850712L)
                .setText(messageStr);
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public TelegramBot init() {
        TelegramBotsApi botsApi = new TelegramBotsApi();


        try {
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return this;
    }

    public static void main(String[] args) {
        TelegramBot bot = new TelegramBot();
        bot.init();
        bot.sendToChat("Я посылаю сообщение из программы!");

    }
}
