package tradeBot.analyze;

import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.criteria.pnl.ProfitCriterion;
import tradeBot.analyze.entities.MACrossoverWithRSIStrategyData;
import tradeBot.analyze.entities.StrategyData;

import java.util.List;

@Component
public class StrategyRun {

    public void run(BarSeries series, BaseStrategy strategy){
        BarSeriesManager manager = new BarSeriesManager(series);
        TradingRecord record = manager.run(strategy);

        AnalysisCriterion criterion = new ProfitCriterion();

        System.out.println(criterion.calculate(series, record));
        System.out.println(strategy.shouldEnter(series.getEndIndex(), record));
        System.out.println(strategy.shouldExit(series.getEndIndex(), record));
    }

    public MACrossoverWithRSIStrategyData chooseBetterMAStrategyByProfit(BarSeries series, List<MACrossoverWithRSIStrategyData> strategies){
        MACrossoverWithRSIStrategyData best = null;
        TradingRecord bestRecord = null;
        BarSeriesManager manager = new BarSeriesManager(series);
        AnalysisCriterion profitCriterion = new ProfitCriterion();

        for(var strategy: strategies){
            if(best == null) {
                best = strategy;
                bestRecord = manager.run(strategy.getStrategy());
            }
            else{
                TradingRecord record = manager.run(strategy.getStrategy());

                if(profitCriterion.calculate(series, record).isGreaterThan(profitCriterion.calculate(series, bestRecord))){
                    best = strategy;
                    bestRecord = record;
                }
            }
        }

        assert best != null;
        best.setRecord(bestRecord);
        return best;
    }

    public void setTradingRecord(BarSeries series, StrategyData strategyData){
        BarSeriesManager manager = new BarSeriesManager(series);
        TradingRecord record = manager.run(strategyData.getStrategy());

        strategyData.setRecord(record);
    }
}
