package tradeBot.telegram.service;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tradeBot.invest.StrategiesSolutions;

public class Tets {

    MsgSender sender;

    public Tets(MsgSender sender){
        this.sender = sender;
    }


    public void send() throws TelegramApiException {
        ApplicationContext context = new AnnotationConfigApplicationContext("tradeBot/invest", "tradeBot/analyze");

        StrategiesSolutions solutionsManager = context.getBean(StrategiesSolutions.class);

        var solutions = solutionsManager.sharesSolutions();

        for(var key: solutions.keySet()){
            sender.send("По " + key + " " + solutions.get(key));
        }

    }
}
