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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tradeBot.invest.shares.SharesDataDistributor;
import tradeBot.telegram.configs.BotConfig;
import tradeBot.telegram.service.pagesManaging.pageUtils.InlineKeyboardBuilder;
import tradeBot.telegram.service.pagesManaging.pageUtils.MessageBuilder;
import tradeBot.telegram.service.pagesManaging.pageUtils.PageManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class TradeBot extends TelegramLongPollingBot {

    final BotConfig config;

    @Autowired
    InstrumentsDataSender instrumentsDataSender;

    @Autowired
    SharesDataDistributor dataDistributor;

    @Autowired
    PageManager pageManager;

    @EventListener(ContextRefreshedEvent.class)
    private void setup() throws TelegramApiException, IOException {
        //instrumentsDataSender.send(this::sendToMe, this::sendMessageToChooseSolutions);
        instrumentsDataSender.setEveryInstrumentsSender(this::sendToMe);
        instrumentsDataSender.setCommonSender(this::sendMessageToChooseSolutions);


        dataDistributor.setSolutionsSender(this::sendSolutions);
    }

    public TradeBot(BotConfig config){
        this.config = config;
    }


    private void sendToMe(String text, InputFile file) throws TelegramApiException {
        MessageBuilder builder = new MessageBuilder();
        execute(builder.createPhotoMessage(null, config.getOwnerId(), text, file));
    }

    private void sendSolutions(PartialBotApiMethod<Message> message) throws TelegramApiException {
//        InlineKeyboardBuilder keyboardBuilder = new InlineKeyboardBuilder();
//        MessageBuilder messageBuilder = new MessageBuilder();
//
//        keyboardBuilder = keyboardBuilder.addButton("нет", callbacks[0]);
//        keyboardBuilder = keyboardBuilder.addButton("да", callbacks[1]);
//        keyboardBuilder.nextRow();
//
//        execute(messageBuilder.createPhotoMessage(keyboardBuilder.build(), config.getOwnerId(), text, pic));
        execute((SendPhoto) message);
    }

    private void sendMessageToChooseSolutions(String[] tickers, double[] prices) throws TelegramApiException {
        String[] args = new String[tickers.length];

        for(int i=0;i<tickers.length;i++){
            args[i] = tickers[i] + "-" + prices[i];
        }


        List<SendMessage> messages = pageManager.execute(null,
                "/chooseSolution")
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

        try{
            if(update.hasMessage()){
                processMessage(update);
            }else if(update.hasCallbackQuery()){
                processCallback(update);
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    private void processMessage(Update update) throws TelegramApiException {
        System.out.println("Message: " + update.getMessage().getText());

        String[] data = update.getMessage().getText().split(" ");
        String page = "";
        String[] args;

        if(data[0].startsWith("/")){
            page = data[0];
            args = Arrays.copyOfRange(data, 1, data.length);
        }else args = data;

        List<PartialBotApiMethod<Message>> messages;

        if(args.length == 0){
            messages = pageManager.execute(update, page);
        }else{
            messages = pageManager.executeWithArgs(update, page, args);
        }

        if(messages == null) return;

        for(var message: messages){
            if(message instanceof SendMessage) execute((SendMessage) message);
            else if(message instanceof SendPhoto) execute((SendPhoto) message);
        }
    }


    private void processCallback(Update update) throws TelegramApiException {
        System.out.println("Callback: " + update.getCallbackQuery().getData());
        String[] data = update.getCallbackQuery().getData().split(" ");
        String page = "";
        String[] args;

        if(data[0].startsWith("/")){
            page = data[0];
            args = Arrays.copyOfRange(data, 1, data.length);
        }else args = data;


        List<PartialBotApiMethod<Message>> messages;

        if(args.length == 0){
            messages = pageManager.executeCallback(update, page);
        }else{
            messages = pageManager.executeCallbackWithArgs(update, page, args);
        }

        if(messages == null) return;

        for(var message: messages){
            if(message instanceof SendMessage) execute((SendMessage) message);
            else if(message instanceof SendPhoto) execute((SendPhoto) message);
        }
    }
}
