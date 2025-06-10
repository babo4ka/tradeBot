package tradeBot.analyze;

import org.springframework.stereotype.Component;
import org.ta4j.core.AnalysisCriterion;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.criteria.pnl.ProfitCriterion;

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

    public StrategyBuilder.StrategyData chooseBetterStrategyByProfit(BarSeries series, List<StrategyBuilder.StrategyData> strategies){
        StrategyBuilder.StrategyData best = null;
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
}
