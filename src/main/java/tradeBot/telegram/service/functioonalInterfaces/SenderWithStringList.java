package tradeBot.telegram.service.functioonalInterfaces;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@FunctionalInterface
public interface SenderWithStringList {

    void send(String[] strings, double[] prices) throws TelegramApiException;
}
