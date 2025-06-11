package tradeBot.invest;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import tradeBot.analyze.MAStrategyBuilder;
import tradeBot.analyze.StrategyRun;
import tradeBot.analyze.entities.MACrossoverWithRSIStrategyData;
import tradeBot.visualize.StrategyVisualizer;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext("tradeBot");

        SharesDataLoader dl = context.getBean(SharesDataLoader.class);
        MAStrategyBuilder sb = context.getBean(MAStrategyBuilder.class);
        StrategyRun sr = context.getBean(StrategyRun.class);
        StrategyVisualizer sv = context.getBean(StrategyVisualizer.class);

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime from = now.minusMonths(6);

        String ticker = "PLZL";

        var candles = dl.loadCandlesData(ticker, from.toInstant(), now.toInstant());
        var series = dl.getBarSeries(ticker, candles, CandleInterval.CANDLE_INTERVAL_DAY);


        List<MACrossoverWithRSIStrategyData> strategies = new ArrayList<>();


        for(int i=1; i<24; i+=2){
            for(int j=20;j<200;j+=4){
                strategies.add(sb.smaCrossoverStrategyWithRSI(series, i, j, 14));
            }
        }

        var strategy = sr.chooseBetterMAStrategyByProfit(series, strategies);

        System.out.println(strategy.getShortMa().getCountOfUnstableBars() + " " + strategy.getLongMA().getCountOfUnstableBars() + " " + strategy.getRsiIndicator().getCountOfUnstableBars());

        sv.visualizeMAStrategy("Стратегия для " + ticker, series, strategy.getRecord(), strategy.getShortMa(), strategy.getLongMA());

        sr.run(series, strategy.getStrategy());
    }
}
