package tradeBot.telegram.service.pagesManaging.pages.solutionPage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import tradeBot.invest.SharesDataLoader;
import tradeBot.invest.TickersList;
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

    @Autowired
    SharesDataLoader sharesDataLoader;

    @Override
    public List<PartialBotApiMethod<Message>> execute(Update update) {
        return getMessages(update);
    }

    @Override
    public List<PartialBotApiMethod<Message>> executeCallback(Update update) {
        return getMessages(update);
    }


    private List<PartialBotApiMethod<Message>> getMessages(Update update){
        MessageBuilder messageBuilder = new MessageBuilder();
        InlineKeyboardBuilder keyboardBuilder = new InlineKeyboardBuilder();

        for(var ticker: TickersList.tickers){
            keyboardBuilder = keyboardBuilder.addButton(ticker, "/solution " + ticker + " " + sharesDataLoader.getInstrumentPrice(ticker)).nextRow();
        }

        return new ArrayList<>(List.of(messageBuilder.createTextMessage(keyboardBuilder.build(), config.getOwnerId(), "Решения по тикерам")));
    }
}
