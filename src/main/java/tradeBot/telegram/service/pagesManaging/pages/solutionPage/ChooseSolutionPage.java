package tradeBot.telegram.service.pagesManaging.pages.solutionPage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import tradeBot.telegram.configs.BotConfig;
import tradeBot.telegram.service.pagesManaging.interfaces.Page;
import tradeBot.telegram.service.pagesManaging.pageUtils.InlineKeyboardBuilder;
import tradeBot.telegram.service.pagesManaging.pageUtils.MessageBuilder;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChooseSolutionPage implements Page {

    @Autowired
    BotConfig config;

    @Override
    public List<PartialBotApiMethod<Message>> executeWithArgs(Update update, String... args) {
        MessageBuilder messageBuilder = new MessageBuilder();
        InlineKeyboardBuilder keyboardBuilder = new InlineKeyboardBuilder();

        for(var ticker: args){
            keyboardBuilder = keyboardBuilder.addButton(ticker, "/solution " + ticker).nextRow();
        }

        return new ArrayList<>(List.of(messageBuilder.createTextMessage(keyboardBuilder.build(), config.getOwnerId(), "Решения по тикерам")));
    }
}
