package tradeBot.invest;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.ta4j.core.BaseStrategy;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import tradeBot.analyze.StrategyBuilder;
import tradeBot.analyze.StrategyRun;
import tradeBot.visualize.StrategyVisualizer;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext("tradeBot");

        SharesDataLoader dl = context.getBean(SharesDataLoader.class);
        StrategyBuilder sb = context.getBean(StrategyBuilder.class);
        StrategyRun sr = context.getBean(StrategyRun.class);
        StrategyVisualizer sv = context.getBean(StrategyVisualizer.class);

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime from = now.minusMonths(6);

        String ticker = "SBERP";

        var candles = dl.loadCandlesData(ticker, from.toInstant(), now.toInstant());
        var series = dl.getBarSeries(ticker, candles, CandleInterval.CANDLE_INTERVAL_DAY);


        List<StrategyBuilder.StrategyData> strategies = new ArrayList<>();


        for(int i=1; i<24; i+=2){
            for(int j=20;j<200;j+=4){
                strategies.add(sb.maCrossoverStrategyWithRSI(series, i, j, 14));
            }
        }

        var strategy = sr.chooseBetterStrategyByProfit(series, strategies);

        System.out.println(strategy.getShortMaCount() + " " + strategy.getLongMaCount() + " " + strategy.getRsiCount());

        sv.visualizeStrategy("Стратегия для " + ticker, series, strategy.getRecord(), strategy.getShortEma(), strategy.getLongEMA());

        sr.run(series, strategy.getStrategy());
    }
}
