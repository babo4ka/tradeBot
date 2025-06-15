package tradeBot.telegram.service.functioonalInterfaces;

import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@FunctionalInterface
public interface SenderWithTextNFile {

    void send(String text, InputFile file) throws TelegramApiException;
}
