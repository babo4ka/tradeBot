package tradeBot.telegram.service;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@FunctionalInterface
public interface MsgSender {

    void send(String text) throws TelegramApiException;
}
