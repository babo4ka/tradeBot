package tradeBot.telegram.service.pagesManaging.pages.solutionPage;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import tradeBot.telegram.service.pagesManaging.interfaces.Page;

import java.util.List;

@Component
public class SolutionPage implements Page {


    @Override
    public List<PartialBotApiMethod<Message>> executeCallbackWithArgs(Update update, String... args) {
        if(args.length == 1){

        }

        return Page.super.executeCallbackWithArgs(update, args);
    }
}
