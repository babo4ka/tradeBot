package tradeBot.telegram.service.pagesManaging.interfaces;


import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public interface Page {

    default List<PartialBotApiMethod<Message>> execute(Update update) throws TelegramApiException {
        return null;
    }

    default List<PartialBotApiMethod<Message>> executeWithArgs(Update update, String...args){
        return null;
    }

    default List<PartialBotApiMethod<Message>> executeCallback(Update update) throws TelegramApiException {
        return null;
    }

    default List<PartialBotApiMethod<Message>> executeCallbackWithArgs(Update update, String...args){
        return null;
    }
}
