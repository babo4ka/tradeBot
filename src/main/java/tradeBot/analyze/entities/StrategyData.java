package tradeBot.analyze.entities;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.TradingRecord;

@Data
@SuperBuilder
public class StrategyData {

    private BaseStrategy strategy;
    private TradingRecord record;

}
