package tradeBot.analyze;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import tradeBot.analyze.entities.MACrossoverWithRSIStrategyData;

@Component
public class MAStrategyBuilder {

    public MACrossoverWithRSIStrategyData emaCrossoverStrategyWithRSI(BarSeries series, int shortMaCount, int longMaCount, int rsiCount){
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator shortEMA = new EMAIndicator(closePrice, shortMaCount);
        EMAIndicator longEMA = new EMAIndicator(closePrice, longMaCount);

        RSIIndicator rsiIndicator = new RSIIndicator(closePrice, rsiCount);

        Rule entryRule = new CrossedUpIndicatorRule(shortEMA, longEMA)
                .and(new OverIndicatorRule(rsiIndicator, DecimalNum.valueOf(50)));

        Rule exitRule = new CrossedDownIndicatorRule(shortEMA, longEMA)
                .or(new CrossedUpIndicatorRule(rsiIndicator, DecimalNum.valueOf(70)))
                .or(new CrossedDownIndicatorRule(rsiIndicator, DecimalNum.valueOf(50)));


        return MACrossoverWithRSIStrategyData.builder()
                .strategy(new BaseStrategy(entryRule, exitRule))
                .shortMa(shortEMA).longMA(longEMA)
                .rsiIndicator(rsiIndicator)
                .build();
    }

    public MACrossoverWithRSIStrategyData smaCrossoverStrategyWithRSI(BarSeries series, int shortMaCount, int longMaCount, int rsiCount){
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        SMAIndicator shortSMA = new SMAIndicator(closePrice, shortMaCount);
        SMAIndicator longSMA = new SMAIndicator(closePrice, longMaCount);

        RSIIndicator rsiIndicator = new RSIIndicator(closePrice, rsiCount);

        Rule entryRule = new OverIndicatorRule(shortSMA, longSMA).and(new OverIndicatorRule(rsiIndicator, DecimalNum.valueOf(50)));
        Rule exitRule = new UnderIndicatorRule(shortSMA, longSMA);


        return MACrossoverWithRSIStrategyData.builder()
                .strategy(new BaseStrategy(entryRule, exitRule))
                .shortMa(shortSMA).longMA(longSMA)
                .rsiIndicator(rsiIndicator)
                .build();
    }

}
