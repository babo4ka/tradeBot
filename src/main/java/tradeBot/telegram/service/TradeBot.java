package tradeBot.telegram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tradeBot.telegram.configs.BotConfig;
import tradeBot.telegram.service.pagesManaging.pageUtils.InlineKeyboardBuilder;
import tradeBot.telegram.service.pagesManaging.pageUtils.MessageBuilder;

import java.io.IOException;
import java.util.List;

@Component
public class TradeBot extends TelegramLongPollingBot {

    final BotConfig config;

    @Autowired
    InstrumentsDataSender t;

//    @EventListener(ContextRefreshedEvent.class)
//    private void setupTets(){
//        t.setSender(this::sendToMe);
//    }

    public TradeBot(BotConfig config){
        this.config = config;
    }


    private void sendToMe(String text, InputFile file) throws TelegramApiException {
        MessageBuilder builder = new MessageBuilder();
        execute(builder.createPhotoMessage(null, 268932900L, text, file));
    }

    private void sendMessageToChooseSolutions(List<String> tickers) throws TelegramApiException {
        MessageBuilder messageBuilder = new MessageBuilder();
        InlineKeyboardBuilder keyboardBuilder = new InlineKeyboardBuilder();

        for(var ticker: tickers){
            keyboardBuilder = keyboardBuilder.addButton(ticker, "/solution " + ticker).nextRow();
        }

        execute(messageBuilder.createTextMessage(keyboardBuilder.build(), 268932900L, "Решения по тикерам"));
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
        //System.out.println(update.getMessage().getChatId());
        try {
            t.send(this::sendToMe, this::sendMessageToChooseSolutions);
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
        System.out.println("msg");
        System.out.println(update.getMessage().getText());
    }


    private void processCallback(Update update){
        System.out.println("cllbck");
        System.out.println(update.getCallbackQuery().getData());
    }
}
