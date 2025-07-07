package tradeBot.invest.shares;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.bars.TimeBarBuilder;
import org.ta4j.core.num.DecimalNum;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.InvestApi;
import tradeBot.invest.ApiDistributor;
import tradeBot.invest.configs.InvestConfig;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class SharesDataLoader {

    final InvestConfig config;

    @Autowired
    ApiDistributor apiDistributor;


    @Autowired
    public SharesDataLoader(InvestConfig config){
        this.config = config;
    }

    public List<HistoricCandle> loadCandlesData(String ticker, Instant from, Instant to, CandleInterval interval){
        assert apiDistributor.getApi() != null;

        String figi = getFigiForShare(ticker, apiDistributor.getApi());

        var unMutableList = apiDistributor.getApi().getMarketDataService().getCandles(figi,
                from, to, interval).join();

        return new ArrayList<>(unMutableList);
    }

    public BarSeries getBarSeries(String ticker, List<HistoricCandle> candles, CandleInterval interval, int maxBars){
        BarSeries series = new BaseBarSeriesBuilder().withName(ticker).build();
        series.setMaximumBarCount(maxBars);

        Duration duration = switch (interval){
            case CANDLE_INTERVAL_DAY -> Duration.ofDays(1);
            case CANDLE_INTERVAL_HOUR -> Duration.ofHours(1);
            case CANDLE_INTERVAL_MONTH -> Duration.of(1, ChronoUnit.MONTHS);
            default -> Duration.ofDays(1);
        };


        for(var candle : candles){
            Instant endTime = ZonedDateTime.ofInstant(
                    Instant.ofEpochSecond(candle.getTime().getSeconds(), candle.getTime().getNanos()),
                    ZoneId.of("Europe/Moscow")).toInstant();
            series.addBar(new TimeBarBuilder()
                    .openPrice(DecimalNum.valueOf(candle.getOpen().getUnits() + candle.getOpen().getNano()/1e9))
                    .closePrice(DecimalNum.valueOf(candle.getClose().getUnits() + candle.getClose().getNano()/1e9))
                    .highPrice(DecimalNum.valueOf(candle.getHigh().getUnits() + candle.getHigh().getNano()/1e9))
                    .lowPrice(DecimalNum.valueOf(candle.getLow().getUnits() + candle.getLow().getNano()/1e9))
                    .volume(DecimalNum.valueOf(candle.getVolume()))
                    .timePeriod(duration)
                    .endTime(endTime)
                    //.endTime(Instant.ofEpochSecond(candle.getTime().getSeconds()))
                    .build());
        }

        return series;
    }

    public boolean hasInstrument(String ticker){
        assert apiDistributor.getApi() != null;

        String figi = getFigiForShare(ticker, apiDistributor.getApi());

        var sandBoxService = apiDistributor.getApi().getSandboxService();
        var portfolio = sandBoxService.getPortfolio(config.getSandboxAcc()).join();
        for(var pos: portfolio.getPositionsList()){
            if(pos.getFigi().equals(figi)) return true;
        }

        return false;
    }


    public static double getInstrumentPrice(String ticker, @NonNull InvestApi api){
        String figi = getFigiForShare(ticker, api);

        var price = api.getMarketDataService().getLastPrices(List.of(figi)).join().get(0).getPrice();

        return price.getUnits() + price.getNano()/1e9;
    }

    public static Quotation getInstrumentPriceAsQuotation(String ticker, @NonNull InvestApi api){
        String figi = getFigiForShare(ticker, api);

        return api.getMarketDataService().getLastPrices(List.of(figi)).join().get(0).getPrice();
    }

    public static String getFigiForShare(String ticker, @NonNull InvestApi api){
        return api.getInstrumentsService().getShareByTicker(ticker, "TQBR").join().getFigi();
    }

    public static String getTickerByFigi(String figi, @NonNull InvestApi api){
        return api.getInstrumentsService().findInstrument(figi).join().get(0).getTicker();
    }
}
