package tradeBot.invest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.backtest.BarSeriesManager;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import tradeBot.analyze.MAStrategyBuilder;
import tradeBot.analyze.StrategyRun;
import tradeBot.visualize.StrategyVisualizer;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class StrategiesSolutions {

    String [] tickers = {"SBERP", "TATNP", "MOEX", "GAZP", "PIKK"};

    @Autowired
    ApplicationContext context;

    public Map<String, String> sharesSolutions(){
        //ApplicationContext context = new AnnotationConfigApplicationContext("tradeBot");

        SharesDataLoader dataLoader = context.getBean(SharesDataLoader.class);
        MAStrategyBuilder strategyBuilder = context.getBean(MAStrategyBuilder.class);
        StrategyRun strategyRun = context.getBean(StrategyRun.class);

        //StrategyVisualizer strategyVisualizer = context.getBean(StrategyVisualizer.class);

        Map<String, String> solutions = new HashMap<>();

        for(var ticker: tickers){
            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime from = now.minusYears(1);

            var candles = dataLoader.loadCandlesData(ticker, from.toInstant(), now.toInstant());
            var barSeries = dataLoader.getBarSeries(ticker, candles, CandleInterval.CANDLE_INTERVAL_DAY);

            var strategyData = strategyBuilder.emaCrossoverStrategyWithRSI(barSeries, 20, 200, 14);

            BarSeriesManager manager = new BarSeriesManager(barSeries);
            TradingRecord record = manager.run(strategyData.getStrategy());

            solutions.put(ticker, strategyData.getStrategy().shouldEnter(barSeries.getEndIndex(), record)?"входим":
                    (strategyData.getStrategy().shouldExit(barSeries.getBeginIndex(), record)?"выходим":"ничего не делаем"));
        }

        return solutions;
    }
}
