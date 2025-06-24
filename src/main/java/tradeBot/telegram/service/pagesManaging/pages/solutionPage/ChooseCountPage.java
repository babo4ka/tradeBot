package tradeBot.telegram.service.pagesManaging.pages.solutionPage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import tradeBot.telegram.configs.BotConfig;
import tradeBot.telegram.service.pagesManaging.interfaces.Page;
import tradeBot.telegram.service.pagesManaging.pageUtils.InlineKeyboardBuilder;
import tradeBot.telegram.service.pagesManaging.pageUtils.MessageBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ChooseCountPage implements Page {

    @Autowired
    BotConfig config;

    private String currentTicker;

    @Override
    public List<PartialBotApiMethod<Message>> executeCallbackWithArgs(Update update, String... args) {
        List<SendMessage> messages = new ArrayList<>();
        InlineKeyboardBuilder keyboardBuilder = new InlineKeyboardBuilder();
        MessageBuilder messageBuilder = new MessageBuilder();

        System.out.println(Arrays.toString(args));

        if(args.length == 1){
            keyboardBuilder = keyboardBuilder.addButton("Назад", "/solution " + args[0]).nextRow();
            currentTicker = args[0];
        }

        messages.add(messageBuilder.createTextMessage(keyboardBuilder.build(), config.getOwnerId(), "Введи количество лотов для входа"));

        return messages.stream().map(e -> (PartialBotApiMethod<Message>) e).toList();
    }


    @Override
    public List<PartialBotApiMethod<Message>> executeWithArgs(Update update, String... args) {
        List<SendMessage> messages = new ArrayList<>();
        InlineKeyboardBuilder keyboardBuilder = new InlineKeyboardBuilder();
        MessageBuilder messageBuilder = new MessageBuilder();

        keyboardBuilder = keyboardBuilder.addButton("Назад", "/chooseSolution").nextRow();
        messages.add(messageBuilder.createTextMessage(keyboardBuilder.build(), config.getOwnerId(), "Вход на " + args[0] + " позиций по " + currentTicker));

        return messages.stream().map(e -> (PartialBotApiMethod<Message>) e).toList();
    }
}
