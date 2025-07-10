package tradeBot.invest.shares;


import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.bars.TimeBarBuilder;
import org.ta4j.core.num.DecimalNum;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import tradeBot.analyze.MAStrategyBuilder;
import tradeBot.analyze.entities.MACrossoverWithRSIStrategyData;
import tradeBot.commonUtils.Pair;
import tradeBot.commonUtils.Triple;
import tradeBot.invest.ApiDistributor;
import tradeBot.invest.TickersList;
import tradeBot.invest.configs.InvestConfig;
import tradeBot.invest.ordersService.CommonOrdersService;
import tradeBot.invest.ordersService.CommonStatisticsService;
import tradeBot.telegram.configs.BotConfig;
import tradeBot.telegram.service.functioonalInterfaces.SenderWithMessage;
import tradeBot.telegram.service.pagesManaging.pageUtils.InlineKeyboardBuilder;
import tradeBot.telegram.service.pagesManaging.pageUtils.MessageBuilder;
import tradeBot.visualize.StrategyVisualizer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Component
@EnableScheduling
@Scope("singleton")
public class SharesDataDistributor {

    @Setter
    private SenderWithMessage messagesSender;

    private final MessageBuilder messageBuilder = new MessageBuilder();
    private InlineKeyboardBuilder keyboardBuilder = new InlineKeyboardBuilder();

    @Autowired
    BotConfig tgBotConfig;

    final InvestConfig investConfig;

    @Autowired
    ApiDistributor apiDistributor;

    final int maxCandlesCount = 200;

    @Autowired
    ApplicationContext context;

    CommonOrdersService ordersService;

    CommonStatisticsService statisticsService;

    @Autowired
    SharesDataLoader sharesDataLoader;


    private final Map<String, Triple<SolutionType, Boolean, Integer>> morningSolutions = new HashMap<>();

    public int getLotsPutByTicker(String ticker){return morningSolutions.get(ticker).getThird();}

    public SolutionType getSolutionTypeByTicker(String ticker){ return morningSolutions.get(ticker).getFirst();}

    @Getter
    public enum SolutionType{
        ENTER("входим"),
        EXIT("выходим");

        private final String solution;

        SolutionType(String solution){
            this.solution = solution;
        }

    }


    private final Map<String, Pair<BarSeries, MACrossoverWithRSIStrategyData>> instrumentsInfo = new HashMap<>();

    private final Map<String, ByteArrayOutputStream> currentPicByTicker = new HashMap<>();

    public Pair<BarSeries, MACrossoverWithRSIStrategyData> getDataByTicker(String ticker) {return instrumentsInfo.get(ticker);}


    public SharesDataDistributor(InvestConfig config,
                                 @Qualifier("OrdersInSandboxService") CommonOrdersService ordersService,
                                 @Qualifier("StatisticsInSandboxService") CommonStatisticsService statisticsService){

        this.investConfig = config;
        this.ordersService = ordersService;
        this.statisticsService = statisticsService;
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
            candles.remove(candles.size()-1);

            BarSeries series = loader.getBarSeries(ticker, candles, CandleInterval.CANDLE_INTERVAL_DAY, maxCandlesCount);

            MACrossoverWithRSIStrategyData strategyData = strategyBuilder.emaCrossoverStrategyWithRSI(series, 9, 21, 14);
            BarSeriesManager manager = new BarSeriesManager(series);
            strategyData.setRecord(manager.run(strategyData.getStrategy()));

            instrumentsInfo.put(ticker, new Pair<>(series, strategyData));
        }
    }


    @Scheduled(cron = "59 59 23 * * ?", zone = "Europe/Moscow")
    //@Scheduled(cron = "20 36 23 * * ?")
    private void update() throws IOException, TelegramApiException {
        morningSolutions.clear();

        ZonedDateTime to = ZonedDateTime.now();
        ZonedDateTime from = to.minusDays(maxCandlesCount);

        StrategyVisualizer visualizer = context.getBean(StrategyVisualizer.class);

        for(var ticker: TickersList.tickers){
            Pair<BarSeries, MACrossoverWithRSIStrategyData> data = instrumentsInfo.get(ticker);

            var candles =  sharesDataLoader.loadCandlesData(ticker, from.toInstant(), to.toInstant(), CandleInterval.CANDLE_INTERVAL_DAY);
            var candle = candles.get(candles.size()-1);

            Instant endTime = ZonedDateTime.ofInstant(
                    Instant.ofEpochSecond(candle.getTime().getSeconds(), candle.getTime().getNanos()),
                    ZoneId.of("Europe/Moscow")).toInstant();


            data.getFirst().addBar(new TimeBarBuilder()
                    .openPrice(DecimalNum.valueOf(candle.getOpen().getUnits() + candle.getOpen().getNano()/1e9))
                    .closePrice(DecimalNum.valueOf(candle.getClose().getUnits() + candle.getClose().getNano()/1e9))
                    .highPrice(DecimalNum.valueOf(candle.getHigh().getUnits() + candle.getHigh().getNano()/1e9))
                    .lowPrice(DecimalNum.valueOf(candle.getLow().getUnits() + candle.getLow().getNano()/1e9))
                    .volume(DecimalNum.valueOf(candle.getVolume()))
                    .timePeriod(Duration.ofDays(1))
                    .endTime(endTime)
                    .build());

            BarSeriesManager manager = new BarSeriesManager(data.getFirst());
            data.getSecond().setRecord(manager.run(data.getSecond().getStrategy()));

            MACrossoverWithRSIStrategyData strategyData = data.getSecond();
            BarSeries series = data.getFirst();

            var pic = visualizer.getMAStrategyPicture(ticker, series,
                    strategyData.getRecord(),
                    strategyData.getShortMa(), strategyData.getLongMA(),
                    strategyData.getRsiIndicator());

            currentPicByTicker.put(ticker, pic);

            System.out.println("for " + ticker + " should enter: " + strategyData.getStrategy().shouldEnter(series.getEndIndex(), strategyData.getRecord()));
            System.out.println("for " + ticker + " should exit: " + strategyData.getStrategy().shouldExit(series.getEndIndex(), strategyData.getRecord()));

            if(!strategyData.getStrategy().shouldEnter(series.getEndIndex(), strategyData.getRecord())
            && !strategyData.getStrategy().shouldExit(series.getEndIndex(), strategyData.getRecord()))
                sendSolutionToChat(ticker, true);
            else{

                if(strategyData.getStrategy().shouldEnter(series.getEndIndex(), strategyData.getRecord())) {
                    morningSolutions.put(ticker, new Triple<>(SolutionType.ENTER, false, 0));
                    sendSolutionToChat(ticker, false);
                }

                else if(strategyData.getStrategy().shouldExit(series.getEndIndex(), strategyData.getRecord())){
                    if(sharesDataLoader.hasInstrument(ticker)){
                        morningSolutions.put(ticker, new Triple<>(SolutionType.EXIT, false, 0));
                        sendSolutionToChat(ticker, false);

                    }else
                        sendSolutionToChat(ticker, true);
                }
            }
        }


    }

    @Scheduled(cron = "0 0 7 * * ?", zone = "Europe/Moscow")
    //@Scheduled(cron = "50 36 23 * * ?")
    private void sendOrders(){

        morningSolutions.keySet().stream().toList().forEach(ticker ->{
            var solution = morningSolutions.get(ticker);

            if(solution.getSecond()){
                switch (solution.getFirst()){
                    case EXIT ->
                        ordersService.postOrderToSell(SharesDataLoader.getFigiForShare(ticker, apiDistributor.getApi()),
                                SharesDataLoader.getInstrumentPriceAsQuotation(ticker, apiDistributor.getApi()));

                    case ENTER ->
                        ordersService.postOrderToBuy(SharesDataLoader.getFigiForShare(ticker, apiDistributor.getApi()),
                                solution.getThird(),
                                SharesDataLoader.getInstrumentPriceAsQuotation(ticker, apiDistributor.getApi()));
                }

                try {
                    sendExecutedSolutionToChat(ticker,
                            solution.getFirst().getSolution(),
                            solution.getThird().longValue(),
                            SharesDataLoader.getInstrumentPrice(ticker, apiDistributor.getApi()));

                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        morningSolutions.clear();
    }

    @Scheduled(cron = "0 30 0 * * ?", zone = "Europe/Moscow")
    //@Scheduled(cron = "25 36 23 * * ?")
    private void sendProfitInfo(){

        Arrays.stream(TickersList.tickers).toList().forEach(ticker -> {
            double profit = statisticsService.countShareProfitByTicker(ticker);

            String text = "Профит по " + ticker + " " + profit + " рублей";

            SendMessage message = messageBuilder.createTextMessage(null, tgBotConfig.getOwnerId(), text);

            try {
                messagesSender.send(message, false);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void sendExecutedSolutionToChat(String ticker, String solution, long quantity, double price) throws TelegramApiException {
        String text = "По " + ticker + " выставлено " + quantity + " лотов на " + solution + " по " + price;

        SendMessage sendMessage = messageBuilder.createTextMessage(null, tgBotConfig.getOwnerId(), text);

        messagesSender.send(sendMessage, false);
    }

    public void sendSolutionToChat(String ticker, boolean wait) throws TelegramApiException {
        String text = "";

        keyboardBuilder = keyboardBuilder.reset();

        if(morningSolutions.containsKey(ticker)){
            var solution = morningSolutions.get(ticker);

            switch (solution.getFirst()){
                case ENTER -> text = "входим";
                case EXIT -> text = "выходим";
            }

            if(solution.getSecond())
                keyboardBuilder.addButton("Отменить решение", "/cancelSolution " + ticker)
                        .addButton("Добрать позиции", "/solution " + ticker + " " + SharesDataLoader.getInstrumentPrice(ticker, apiDistributor.getApi()) + " 1 " + text).nextRow();
            else
                keyboardBuilder.addButton("Принять решение", "/solution " + ticker + " " + SharesDataLoader.getInstrumentPrice(ticker, apiDistributor.getApi()) + " 1 " + text)
                        .nextRow();
        }else text = " - ждём...";

        SendPhoto message = messageBuilder.createPhotoMessage(keyboardBuilder.build(),
                tgBotConfig.getOwnerId(),
                "Решение по " + ticker + " " + text + (wait?"":", подтверждаем?"),
                new InputFile(new ByteArrayInputStream(currentPicByTicker.get(ticker).toByteArray()), "file"));


        messagesSender.send(message, true);
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

    @EventListener(ContextRefreshedEvent.class)
    private void subscribeTrades(){
        apiDistributor.getApi().getOrdersStreamService().subscribeTrades(
                response -> {
                    System.out.println("Sandbox Order Update: " + response);
                    response.getOrderTrades().getTradesList().forEach(orderTrade -> {
                        String ticker = SharesDataLoader.getTickerByFigi(response.getOrderTrades().getFigi(), apiDistributor.getApi());

                        String text = "Исполнено по " + ticker + " в количестве " + orderTrade.getQuantity()
                                + " по " + (orderTrade.getPrice().getUnits() + orderTrade.getPrice().getNano()/1e9);

                        SendMessage message = messageBuilder.createTextMessage(null, tgBotConfig.getOwnerId(), text);

                        try {
                            messagesSender.send(message, false);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    });

                },
                throwable -> System.err.println("Stream error: " + throwable),
                List.of(investConfig.isSandbox()?investConfig.getSandboxAcc():investConfig.getUsualAcc())
        );
    }

}
