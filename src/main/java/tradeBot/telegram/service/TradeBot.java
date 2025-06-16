package tradeBot.telegram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tradeBot.invest.TickersList;
import tradeBot.telegram.configs.BotConfig;
import tradeBot.telegram.service.pagesManaging.pageUtils.InlineKeyboardBuilder;
import tradeBot.telegram.service.pagesManaging.pageUtils.MessageBuilder;
import tradeBot.telegram.service.pagesManaging.pageUtils.PageManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class TradeBot extends TelegramLongPollingBot {

    final BotConfig config;

    @Autowired
    InstrumentsDataSender t;

    @Autowired
    PageManager pageManager;

//    @EventListener(ContextRefreshedEvent.class)
//    private void setupTets(){
//        t.setSender(this::sendToMe);
//    }

    public TradeBot(BotConfig config){
        this.config = config;
    }


    private void sendToMe(String text, InputFile file) throws TelegramApiException {
        MessageBuilder builder = new MessageBuilder();
        execute(builder.createPhotoMessage(null, config.getOwnerId(), text, file));
    }

    private void sendMessageToChooseSolutions(String[] tickers) throws TelegramApiException {
        List<SendMessage> messages = pageManager.executeWithArgs(null,
                "/chooseSolution",
                tickers)
                .stream().map(e -> (SendMessage)e).toList();

        for(var msg: messages){
            execute(msg);
        }
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
            try {
                processCallback(update);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void processMessage(Update update){
        System.out.println("msg");
        System.out.println(update.getMessage().getText());
    }


    private void processCallback(Update update) throws TelegramApiException {
        String[] data = update.getCallbackQuery().getData().split(" ");
        String page = "";
        String[] args;

        if(data[0].startsWith("/")){
            page = data[0];
            args = Arrays.copyOfRange(data, 1, data.length);
        }else{
            args = data;
        }

        List<PartialBotApiMethod<Message>> messages;

        if(args.length == 0){
            messages = pageManager.executeCallback(update, page);
        }else{
            messages = pageManager.executeCallbackWithArgs(update, page, args);
        }

        for(var message: messages){
            if(message instanceof SendMessage) execute((SendMessage) message);
            else if(message instanceof SendPhoto) execute((SendPhoto) message);
        }
    }
}
