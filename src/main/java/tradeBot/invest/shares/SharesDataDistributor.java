package tradeBot.invest.shares;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.bars.TimeBarBuilder;
import org.ta4j.core.num.DecimalNum;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.core.InvestApi;
import tradeBot.analyze.MAStrategyBuilder;
import tradeBot.analyze.entities.MACrossoverWithRSIStrategyData;
import tradeBot.commonUtils.Pair;
import tradeBot.invest.TickersList;
import tradeBot.invest.configs.InvestConfig;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@EnableScheduling
public class SharesDataDistributor {

    final InvestConfig config;

    final InvestApi api;

    final int maxCandlesCount = 5;

    @Autowired
    ApplicationContext context;



    private Map<String, Pair<BarSeries, MACrossoverWithRSIStrategyData>> instrumentsInfo = new HashMap<>();

    public Pair<BarSeries, MACrossoverWithRSIStrategyData> getDataByTicker(String ticker) {return instrumentsInfo.get(ticker);}

    @Autowired
    public SharesDataDistributor(InvestConfig config){
        this.config = config;
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

            MACrossoverWithRSIStrategyData strategyData = strategyBuilder.emaCrossoverStrategyWithRSI(series, 5, 10, 14);
            BarSeriesManager manager = new BarSeriesManager(series);
            strategyData.setRecord(manager.run(strategyData.getStrategy()));

            instrumentsInfo.put(ticker, new Pair<>(series, strategyData));
        }
    }

    //@Scheduled(cron = "0 * * * * ?")
    private void update(){
        SharesDataLoader loader = context.getBean(SharesDataLoader.class);

        ZonedDateTime to = ZonedDateTime.now();
        ZonedDateTime from = to.minusDays(maxCandlesCount);

        for(var ticker: TickersList.tickers){
            Pair<BarSeries, MACrossoverWithRSIStrategyData> data = instrumentsInfo.get(ticker);
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            var candle = loader.loadCandlesData(ticker, from.toInstant(), to.toInstant(), CandleInterval.CANDLE_INTERVAL_DAY).get(1);

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
        }
    }

    private void reloadData(){
        loadData();
    }
}
