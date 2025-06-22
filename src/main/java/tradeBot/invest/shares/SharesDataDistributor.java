package tradeBot.invest.shares;


import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.backtest.BarSeriesManager;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.core.InvestApi;
import tradeBot.analyze.MAStrategyBuilder;
import tradeBot.analyze.entities.MACrossoverWithRSIStrategyData;
import tradeBot.commonUtils.Pair;
import tradeBot.commonUtils.Triple;
import tradeBot.invest.TickersList;
import tradeBot.invest.configs.InvestConfig;
import tradeBot.telegram.configs.BotConfig;
import tradeBot.telegram.service.functioonalInterfaces.SenderWithTextFileNCallback;
import tradeBot.telegram.service.pagesManaging.pageUtils.InlineKeyboardBuilder;
import tradeBot.telegram.service.pagesManaging.pageUtils.MessageBuilder;
import tradeBot.visualize.StrategyVisualizer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@EnableScheduling
@Scope("singleton")
public class SharesDataDistributor {

    @Setter
    private SenderWithTextFileNCallback solutionsSender;

    private final MessageBuilder messageBuilder = new MessageBuilder();
    private InlineKeyboardBuilder keyboardBuilder = new InlineKeyboardBuilder();

    @Autowired
    BotConfig tgBotConfig;

    final InvestConfig investConfig;
    final InvestApi api;
    final int maxCandlesCount = 200;

    @Autowired
    ApplicationContext context;


    private final Map<String, Triple<SolutionType, Boolean, Integer>> morningSolutions = new HashMap<>();

    public int getLotsPutByTicker(String ticker){
        return morningSolutions.get(ticker).getThird();
    }

    public enum SolutionType{
        ENTER, EXIT
    }


    private final Map<String, Pair<BarSeries, MACrossoverWithRSIStrategyData>> instrumentsInfo = new HashMap<>();

    private final Map<String, ByteArrayOutputStream> currentPicByTicker = new HashMap<>();

    public Pair<BarSeries, MACrossoverWithRSIStrategyData> getDataByTicker(String ticker) {return instrumentsInfo.get(ticker);}

    //@Autowired
    public SharesDataDistributor(InvestConfig config){
        this.investConfig = config;
        api = InvestApi.create(config.getSandboxToken());
    }

    @EventListener(ContextRefreshedEvent.class)
    private void loadData(){
        instrumentsInfo.clear();
        SharesDataLoader loader = context.getBean(SharesDataLoader.class);
        MAStrategyBuilder strategyBuilder = context.getBean(MAStrategyBuilder.class);

        ZonedDateTime to = ZonedDateTime.now();
        ZonedDateTime from = to.minusDays(maxCandlesCount);

        for(var ticker: TickersList.tickers){
            var candles = loader.loadCandlesData(ticker, from.toInstant(), to.toInstant(), CandleInterval.CANDLE_INTERVAL_DAY);

            BarSeries series = loader.getBarSeries(ticker, candles, CandleInterval.CANDLE_INTERVAL_DAY, maxCandlesCount);

            MACrossoverWithRSIStrategyData strategyData = strategyBuilder.emaCrossoverStrategyWithRSI(series, 9, 21, 14);
            BarSeriesManager manager = new BarSeriesManager(series);
            strategyData.setRecord(manager.run(strategyData.getStrategy()));

            instrumentsInfo.put(ticker, new Pair<>(series, strategyData));
        }
    }

    @Scheduled(cron = "51 48 13 * * ?")
    private void update() throws IOException, TelegramApiException {
        morningSolutions.clear();

        SharesDataLoader loader = context.getBean(SharesDataLoader.class);

        ZonedDateTime to = ZonedDateTime.now();
        ZonedDateTime from = to.minusDays(maxCandlesCount);

        StrategyVisualizer visualizer = context.getBean(StrategyVisualizer.class);

        for(var ticker: TickersList.tickers){
            Pair<BarSeries, MACrossoverWithRSIStrategyData> data = instrumentsInfo.get(ticker);

            var candles =  loader.loadCandlesData(ticker, from.toInstant(), to.toInstant(), CandleInterval.CANDLE_INTERVAL_DAY);
            var candle = candles.get(candles.size()-1);

            Instant endTime = ZonedDateTime.ofInstant(
                    Instant.ofEpochSecond(candle.getTime().getSeconds(), candle.getTime().getNanos()),
                    ZoneId.of("Europe/Moscow")).toInstant();


//            data.getFirst().addBar(new TimeBarBuilder()
//                    .openPrice(DecimalNum.valueOf(candle.getOpen().getUnits() + candle.getOpen().getNano()/1e9))
//                    .closePrice(DecimalNum.valueOf(candle.getClose().getUnits() + candle.getClose().getNano()/1e9))
//                    .highPrice(DecimalNum.valueOf(candle.getHigh().getUnits() + candle.getHigh().getNano()/1e9))
//                    .lowPrice(DecimalNum.valueOf(candle.getLow().getUnits() + candle.getLow().getNano()/1e9))
//                    .volume(DecimalNum.valueOf(candle.getVolume()))
//                    .timePeriod(Duration.ofDays(1))
//                    .endTime(endTime)
//                    .build());

            BarSeriesManager manager = new BarSeriesManager(data.getFirst());
            data.getSecond().setRecord(manager.run(data.getSecond().getStrategy()));

            MACrossoverWithRSIStrategyData strategyData = data.getSecond();
            BarSeries series = data.getFirst();

            if(!strategyData.getStrategy().shouldEnter(series.getEndIndex(), strategyData.getRecord())
            && !strategyData.getStrategy().shouldExit(series.getEndIndex(), strategyData.getRecord())){
            }else{
                var pic = visualizer.getMAStrategyPicture(ticker, series,
                        strategyData.getRecord(),
                        strategyData.getShortMa(), strategyData.getLongMA(),
                        strategyData.getRsiIndicator());

                currentPicByTicker.put(ticker, pic);

                if(strategyData.getStrategy().shouldEnter(series.getEndIndex(), strategyData.getRecord()))
                    morningSolutions.put(ticker, new Triple<>(SolutionType.ENTER, false, 0));
                else if(strategyData.getStrategy().shouldExit(series.getEndIndex(), strategyData.getRecord()))
                    morningSolutions.put(ticker, new Triple<>(SolutionType.EXIT, false, 0));


                sendToChat(ticker);
            }
        }
    }

    public void sendToChat(String ticker) throws TelegramApiException {
        String text = "";

        SharesDataLoader loader = context.getBean(SharesDataLoader.class);

        keyboardBuilder = keyboardBuilder.reset();

        if(morningSolutions.containsKey(ticker)){
            var solution = morningSolutions.get(ticker);

            switch (solution.getFirst()){
                case ENTER -> text = "входим";
                case EXIT -> text = "выходим";
            }

            if(solution.getSecond())
                keyboardBuilder.addButton("Отменить решение", "/cancelSolution " + ticker)
                        .addButton("Добрать позиции", "/solution " + ticker + " " + loader.getInstrumentPrice(ticker) + " 1 " + text).nextRow();
            else
                keyboardBuilder.addButton("Нет", "/solution " + ticker + " " + loader.getInstrumentPrice(ticker) + " 0 " + text)
                        .addButton("Да", "/solution " + ticker + " " + loader.getInstrumentPrice(ticker) + " 1 " + text)
                        .nextRow();
        }

        SendPhoto message = messageBuilder.createPhotoMessage(keyboardBuilder.build(),
                tgBotConfig.getOwnerId(),
                "Решение по " + ticker + " " + text + ", подтверждаем?",
                new InputFile(new ByteArrayInputStream(currentPicByTicker.get(ticker).toByteArray()), "file"));


        solutionsSender.send(message);

    }


    public void setLotsCount(String ticker, int count){
        if(morningSolutions.containsKey(ticker)){
            morningSolutions.get(ticker).setSecond(true);
            morningSolutions.get(ticker).setThird(morningSolutions.get(ticker).getThird() + count);
        }

    }

    public void cancelForMorning(String ticker){
        if(morningSolutions.containsKey(ticker)) {
            morningSolutions.get(ticker).setSecond(false);
            morningSolutions.get(ticker).setThird(0);
        }
    }

//    private void reloadData(){
//        loadData();
//    }
}
