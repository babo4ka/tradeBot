package tradeBot.analyze.entities;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.num.Num;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Data
public class MACrossoverWithRSIStrategyData extends StrategyData{
    private Indicator<Num> shortMa;
    private Indicator<Num> longMA;
    private RSIIndicator rsiIndicator;
}
