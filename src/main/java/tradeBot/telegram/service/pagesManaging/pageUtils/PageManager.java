package tradeBot.telegram.service.pagesManaging.pageUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tradeBot.telegram.service.pagesManaging.interfaces.Page;
import tradeBot.telegram.service.pagesManaging.pages.solutionPage.CancelSolution;
import tradeBot.telegram.service.pagesManaging.pages.solutionPage.ChooseCountPage;
import tradeBot.telegram.service.pagesManaging.pages.solutionPage.ChooseSolutionPage;
import tradeBot.telegram.service.pagesManaging.pages.solutionPage.SolutionPage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PageManager {

    @Autowired
    ApplicationContext context;

    private final Map<String, Page> pages = new HashMap<>();

    @EventListener(ContextRefreshedEvent.class)
    private void setupPages(){
        pages.put("/chooseSolution", context.getBean(ChooseSolutionPage.class));
        pages.put("/solution", context.getBean(SolutionPage.class));
        pages.put("/chooseCount", context.getBean(ChooseCountPage.class));
        pages.put("/cancelSolution", context.getBean(CancelSolution.class));
    }

    private String lastCalledPage;

    public List<PartialBotApiMethod<Message>> execute(Update update, String pageName) throws TelegramApiException {
        if(!pageName.isEmpty()) lastCalledPage = pageName;
        else pageName = lastCalledPage;

        return pages.get(pageName).execute(update);
    }

    public List<PartialBotApiMethod<Message>> executeWithArgs(Update update, String pageName, String...args){
        if(!pageName.isEmpty()) lastCalledPage = pageName;
        else pageName = lastCalledPage;

        return pages.get(pageName).executeWithArgs(update, args);
    }

    public List<PartialBotApiMethod<Message>> executeCallback(Update update, String pageName) throws TelegramApiException {
        if(!pageName.isEmpty()) lastCalledPage = pageName;
        else pageName = lastCalledPage;

        return pages.get(pageName).executeCallback(update);
    }

    public List<PartialBotApiMethod<Message>> executeCallbackWithArgs(Update update, String pageName, String...args){
        if(!pageName.isEmpty()) lastCalledPage = pageName;
        else pageName = lastCalledPage;

        return pages.get(pageName).executeCallbackWithArgs(update, args);
    }

}
