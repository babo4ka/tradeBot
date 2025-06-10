package tradeBot.invest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.bars.TimeBarBuilder;
import org.ta4j.core.num.DecimalNum;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.core.InvestApi;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class SharesDataLoader {
    @Autowired
    InvestConfig config;

    public List<HistoricCandle> loadCandlesData(String ticker, Instant from, Instant to){
        InvestApi api = InvestApi.create(config.getSandboxToken());

        String figi = api.getInstrumentsService().findInstrument(ticker)
                .join().stream()
                .filter(i->i.getFigi().startsWith("BBG00"))
                .filter(i->i.getInstrumentType().equals("share"))
                .findFirst().orElseThrow().getFigi();

        return api.getMarketDataService().getCandles(figi,
                from, to, CandleInterval.CANDLE_INTERVAL_DAY).join();
    }

    public BarSeries getBarSeries(String ticker, List<HistoricCandle> candles, CandleInterval interval){
        BarSeries series = new BaseBarSeriesBuilder().withName(ticker).build();

        Duration duration = switch (interval){
            case CANDLE_INTERVAL_DAY -> Duration.ofDays(1);
            case CANDLE_INTERVAL_HOUR -> Duration.ofHours(1);
            case CANDLE_INTERVAL_MONTH -> Duration.of(1, ChronoUnit.MONTHS);
            default -> Duration.ofDays(1);
        };

        for(var candle : candles){
            series.addBar(new TimeBarBuilder()
                    .openPrice(DecimalNum.valueOf(candle.getOpen().getUnits() + candle.getOpen().getNano()/1e9))
                    .closePrice(DecimalNum.valueOf(candle.getClose().getUnits() + candle.getClose().getNano()/1e9))
                    .highPrice(DecimalNum.valueOf(candle.getHigh().getUnits() + candle.getHigh().getNano()/1e9))
                    .lowPrice(DecimalNum.valueOf(candle.getLow().getUnits() + candle.getLow().getNano()/1e9))
                    .volume(DecimalNum.valueOf(candle.getVolume()))
                    .timePeriod(duration)
                    .endTime(Instant.ofEpochSecond(candle.getTime().getSeconds()))
                    .build());
        }

        return series;
    }
}
