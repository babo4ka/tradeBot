package tradeBot.telegram.service.pagesManaging.pageUtils;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import tradeBot.telegram.service.pagesManaging.interfaces.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageManager {

    private final Map<String, Page> pages = new HashMap<>(){

    };

    public List<PartialBotApiMethod<Message>> execute(Update update, String pageName){
        return pages.get(pageName).execute(update);
    }

    public List<PartialBotApiMethod<Message>> executeWithArgs(Update update, String pageName, String...args){
        return pages.get(pageName).executeWithArgs(update, args);
    }

    public List<PartialBotApiMethod<Message>> executeCallback(Update update, String pageName){
        return pages.get(pageName).executeCallback(update);
    }

    public List<PartialBotApiMethod<Message>> executeCallbackWithArgs(Update update, String pageName, String...args){
        return pages.get(pageName).executeCallbackWithArgs(update, args);
    }

}
