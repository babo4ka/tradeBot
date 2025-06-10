package tradeBot.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

@Component
public class StrategyBuilder {

    public StrategyData maCrossoverStrategyWithRSI(BarSeries series, int shortMaCount, int longMaCount, int rsiCount){
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator shortEMA = new EMAIndicator(closePrice, shortMaCount);
        EMAIndicator longEMA = new EMAIndicator(closePrice, longMaCount);

        RSIIndicator rsiIndicator = new RSIIndicator(closePrice, rsiCount);

        Rule entryRule = new OverIndicatorRule(shortEMA, longEMA).and(new OverIndicatorRule(rsiIndicator, DecimalNum.valueOf(50)));
        Rule exitRule = new UnderIndicatorRule(shortEMA, longEMA);

        StrategyData data = new StrategyData();
        data.setStrategy(new BaseStrategy(entryRule, exitRule));
        data.setShortEma(shortEMA);
        data.setLongEMA(longEMA);
        data.setShortMaCount(shortMaCount);
        data.setLongMaCount(longMaCount);
        data.setRsiIndicator(rsiIndicator);
        data.setRsiCount(rsiCount);

        return data;
    }

    @Data
    public static class StrategyData{
        private BaseStrategy strategy;
        private int shortMaCount;
        private int longMaCount;
        private int rsiCount;

        private EMAIndicator shortEma;
        private EMAIndicator longEMA;
        private RSIIndicator rsiIndicator;

        private TradingRecord record;
    }
}
