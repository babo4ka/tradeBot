package tradeBot.telegram.service.pagesManaging.pages.solutionPage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import tradeBot.invest.shares.SharesDataDistributor;
import tradeBot.telegram.configs.BotConfig;
import tradeBot.telegram.service.pagesManaging.interfaces.Page;
import tradeBot.telegram.service.pagesManaging.pageUtils.InlineKeyboardBuilder;
import tradeBot.telegram.service.pagesManaging.pageUtils.MessageBuilder;

import java.util.ArrayList;
import java.util.List;

@Component
public class CancelSolution implements Page {

    @Autowired
    SharesDataDistributor sharesDataDistributor;

    @Autowired
    BotConfig config;

    @Override
    public List<PartialBotApiMethod<Message>> executeCallbackWithArgs(Update update, String... args) {
        List<SendMessage> messages = new ArrayList<>();
        InlineKeyboardBuilder keyboardBuilder = new InlineKeyboardBuilder();
        MessageBuilder messageBuilder = new MessageBuilder();

        sharesDataDistributor.cancelForMorning(args[0]);

        keyboardBuilder = keyboardBuilder
                .addButton("Назад", "/chooseSolution").nextRow();

        messages.add(messageBuilder.createTextMessage
                (keyboardBuilder.build(), config.getOwnerId(),
                        "Решение по " + args[0] + " отменено"));

        return messages.stream().map(e -> (PartialBotApiMethod<Message>) e).toList();
    }
}
