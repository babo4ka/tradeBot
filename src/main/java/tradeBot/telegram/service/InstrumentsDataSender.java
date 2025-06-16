package tradeBot.telegram.service;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tradeBot.invest.StrategiesSolutions;
import tradeBot.telegram.service.functioonalInterfaces.SenderWithStringList;
import tradeBot.telegram.service.functioonalInterfaces.SenderWithTextNFile;

import java.io.*;

@Component
public class InstrumentsDataSender {

    @Setter
    SenderWithTextNFile sender;

    @Autowired
    ApplicationContext context;

    public void send(SenderWithTextNFile everyInstrumentsSender, SenderWithStringList commonSender) throws TelegramApiException, IOException {
        StrategiesSolutions solutionsManager = context.getBean(StrategiesSolutions.class);

        var solutions = solutionsManager.sharesSolutions();


        for(var key: solutions.keySet()){
            String solution = solutions.get(key).getFirst();
            ByteArrayOutputStream image = solutions.get(key).getSecond();
            InputFile file = new InputFile(new ByteArrayInputStream(image.toByteArray()), "file");
            everyInstrumentsSender.send("По " + key + " " + solution, file);
        }

        commonSender.send(solutions.keySet().toArray(new String[0]));
    }
}
