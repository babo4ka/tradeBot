package tradeBot.telegram.service.pagesManaging.pages.solutionPage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tradeBot.invest.shares.SharesDataDistributor;
import tradeBot.invest.shares.SharesDataLoader;
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

    @Autowired
    SharesDataDistributor sharesDataDistributor;

    @Override
    public List<PartialBotApiMethod<Message>> execute(Update update) throws TelegramApiException {
        return getMessages(update);
    }

    @Override
    public List<PartialBotApiMethod<Message>> executeCallback(Update update) throws TelegramApiException {
        return getMessages(update);
    }


    private List<PartialBotApiMethod<Message>> getMessages(Update update) throws TelegramApiException {
        for(var ticker: TickersList.tickers){
            sharesDataDistributor.sendToChat(ticker);
        }
        return null;
    }
}
