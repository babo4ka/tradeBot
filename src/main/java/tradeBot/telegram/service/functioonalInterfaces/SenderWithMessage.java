package tradeBot.telegram.service.functioonalInterfaces;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@FunctionalInterface
public interface SenderWithMessage {

    void send(PartialBotApiMethod<Message> message) throws TelegramApiException;
}
