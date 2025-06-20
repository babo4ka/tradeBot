package tradeBot.telegram.service;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tradeBot.analyze.strategiesTest.StrategiesTest;
import tradeBot.invest.StrategiesSolutions;
import tradeBot.telegram.service.functioonalInterfaces.SenderWithStringList;
import tradeBot.telegram.service.functioonalInterfaces.SenderWithTextNFile;

import java.io.*;

@Component
@EnableScheduling
public class InstrumentsDataSender {

    @Setter
    SenderWithTextNFile everyInstrumentsSender;
    @Setter
    SenderWithStringList commonSender;


    @Autowired
    StrategiesSolutions solutionsManager;

    //@Scheduled(cron = "0 * * * * ?")
    //@Scheduled(fixedDelay = 60000)
    public void send() throws TelegramApiException, IOException {
        var solutions = solutionsManager.sharesSolutions();


        for(var key: solutions.keySet()){
            String solution = solutions.get(key).getFirst();
            ByteArrayOutputStream image = solutions.get(key).getSecond();
            InputFile file = new InputFile(new ByteArrayInputStream(image.toByteArray()), "file");
            everyInstrumentsSender.send("По " + key + " " + solution, file);
        }

        String[] tickers = solutions.keySet().toArray(new String[0]);
        double[] prices = new double[tickers.length];

        for(int i=0;i<tickers.length;i++){
            prices[i] = solutions.get(tickers[i]).getThird();
        }


        commonSender.send(solutions.keySet().toArray(new String[0]), prices);
    }
}
