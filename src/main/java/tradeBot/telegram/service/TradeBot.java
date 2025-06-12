package tradeBot.telegram.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tradeBot.telegram.configs.BotConfig;

@Component
public class TradeBot extends TelegramLongPollingBot {

    final BotConfig config;

    public TradeBot(BotConfig config){
        this.config = config;
    }

    Tets t = new Tets(this::sendToMe);

    private void sendToMe(String text) throws TelegramApiException {
        SendMessage sm = new SendMessage();
        sm.setChatId(268932900L);
        sm.setText(text);
        execute(sm);
    }


    @Override
    public String getBotUsername() {
        return config.getName();
    }
    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println(update.getMessage().getChatId());
        try {
            t.send();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


}
