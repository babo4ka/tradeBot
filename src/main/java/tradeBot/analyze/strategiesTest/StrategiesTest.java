package tradeBot.analyze.strategiesTest;

import org.ta4j.core.AnalysisCriterion;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.criteria.pnl.ProfitCriterion;
import tradeBot.analyze.MAStrategyBuilder;

public class StrategiesTest {

    public void testMAStrategies(BarSeries series, Strategy[] strategies){
        BarSeriesManager manager = new BarSeriesManager(series);
        AnalysisCriterion profitCriterion = new ProfitCriterion();

        double b = 0;
        int id = 0;

        for(int i=0;i<strategies.length;i++){
            TradingRecord record = manager.run(strategies[i]);
            double p = profitCriterion.calculate(series, record).doubleValue();

            System.out.println("curr prof: " + p);

            if (p > b) {
                b = p;
                id = i;
            }

        }

        System.out.println("best id : " + id);
    }

    private void testMAs(int[] shortema, int[] longema, MAStrategyBuilder builder, BarSeries series){
        BarSeriesManager manager = new BarSeriesManager(series);
        AnalysisCriterion profitCriterion = new ProfitCriterion();

        double b = 0;
        int id = 0;

        for(int i=0;i<shortema.length;i++){
            Strategy s = builder.emaCrossoverStrategyWithRSI(series, shortema[i], longema[i], 14).getStrategy();
            TradingRecord record = manager.run(s);
            double p = profitCriterion.calculate(series, record).doubleValue();
            System.out.println("Curr prof: " + p);
            if(p > b){
                b = p;
                id = i;
            }
        }

        System.out.println("best id : " + id);
    }
}
