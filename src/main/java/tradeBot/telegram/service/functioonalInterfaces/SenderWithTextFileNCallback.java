package tradeBot.telegram.service.functioonalInterfaces;

import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@FunctionalInterface
public interface SenderWithTextFileNCallback {

    void send(String text, InputFile file, String[] callbacks) throws TelegramApiException;
}
