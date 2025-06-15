package tradeBot.telegram.service;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tradeBot.invest.StrategiesSolutions;
import tradeBot.visualize.StrategyVisualizer;

import java.io.*;

@Component
public class Tets {

    @Setter
    MsgSender sender;

    @Autowired
    ApplicationContext context;

    public void send() throws TelegramApiException, IOException {
        StrategiesSolutions solutionsManager = context.getBean(StrategiesSolutions.class);

        var solutions = solutionsManager.sharesSolutions();


        for(var key: solutions.keySet()){
            String solution = solutions.get(key).getFirst();
            ByteArrayOutputStream image = solutions.get(key).getSecond();
            InputFile file = new InputFile(new ByteArrayInputStream(image.toByteArray()), "file");
            sender.send("По " + key + " " + solution, file);
        }

    }
}
