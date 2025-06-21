package tradeBot.telegram.service.pagesManaging.pages.solutionPage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import tradeBot.invest.shares.SharesDataLoader;
import tradeBot.telegram.configs.BotConfig;
import tradeBot.telegram.service.pagesManaging.interfaces.Page;
import tradeBot.telegram.service.pagesManaging.pageUtils.InlineKeyboardBuilder;
import tradeBot.telegram.service.pagesManaging.pageUtils.MessageBuilder;

import java.util.ArrayList;
import java.util.List;

@Component
public class SolutionPage implements Page {
    @Autowired
    BotConfig config;

    @Autowired
    SharesDataLoader sharesDataLoader;

    @Override
    public List<PartialBotApiMethod<Message>> executeCallbackWithArgs(Update update, String... args) {
        List<SendMessage> messages = new ArrayList<>();
        InlineKeyboardBuilder keyboardBuilder = new InlineKeyboardBuilder();
        MessageBuilder messageBuilder = new MessageBuilder();

        //if(args.length == 1){
        keyboardBuilder = keyboardBuilder
                .addButton("Назад", "/chooseSolution")
                .addButton(args[3].equals("входим")?"вход":"выход", "/chooseCount " + args[0]).nextRow();

        messages.add(messageBuilder.createTextMessage(keyboardBuilder.build(), config.getOwnerId(), "Решение по " + args[0] + " цена за лот " + args[1]));
       // }

        return messages.stream().map(e -> (PartialBotApiMethod<Message>) e).toList();
    }
}
