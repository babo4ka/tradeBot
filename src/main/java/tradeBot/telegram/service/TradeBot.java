package tradeBot.telegram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tradeBot.telegram.configs.BotConfig;

import java.awt.image.BufferedImage;
import java.io.IOException;

@Component
public class TradeBot extends TelegramLongPollingBot {

    final BotConfig config;

    @Autowired
    Tets t;

    @EventListener(ContextRefreshedEvent.class)
    private void setupTets(){
        t.setSender(this::sendToMe);
    }

    public TradeBot(BotConfig config){
        this.config = config;
    }


    private void sendToMe(String text, InputFile file) throws TelegramApiException {
        SendPhoto sp = new SendPhoto();
        sp.setChatId(268932900L);
        sp.setCaption(text);
        sp.setPhoto(file);
        sp.setPhoto(file);
        execute(sp);
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
        } catch (TelegramApiException | IOException e) {
            throw new RuntimeException(e);
        }


        if(update.hasMessage()){
            processMessage(update);
        }else if(update.hasCallbackQuery()){
            processCallback(update);
        }
    }


    private void processMessage(Update update){

    }


    private void processCallback(Update update){

    }
}
