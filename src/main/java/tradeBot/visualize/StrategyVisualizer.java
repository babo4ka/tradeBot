package tradeBot.visualize;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPolygonAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Component
@Scope("singleton")
public class StrategyVisualizer {

    private ByteArrayOutputStream chartOutput;

    private long startX = -1;
    private long endX = -1;

    private double maxPrice = -1;
    private double maxRsi = -1;

    private final double[] rsiCheckpoints = {30, 50, 70};


    public void visualizeMAStrategy(
            String title,
            BarSeries series, TradingRecord record,
            Indicator<Num> shortIndicator, Indicator<Num> longIndicator,
            Indicator<Num> rsiIndicator
    ) throws IOException {
        maxPrice = -1;
        maxRsi = -1;

        startX = -1;
        endX = -1;


        OHLCDataset candleDataset = createCandleDataset(series);

        XYDataset shortEmaDataset = createIndicatorDataset(series, shortIndicator, "Short MA", false);
        XYDataset longEmaDataset = createIndicatorDataset(series, longIndicator, "Long MA", false);

        List<Integer> entryIndexes = new ArrayList<>();
        List<Integer> exitIndexes = new ArrayList<>();

        series.getBarData().forEach(e -> {
            double closePrice = e.getClosePrice().doubleValue();
            if(closePrice > maxPrice) maxPrice = closePrice;
        });

        maxRsi = maxPrice / 3;

        XYDataset rsiDataset = createIndicatorDataset(series, rsiIndicator, "RSI", true);

        for(var position: record.getPositions()){
            entryIndexes.add(position.getEntry().getIndex());
            exitIndexes.add(position.getExit().getIndex());
        }


        XYDataset entryDataset = createTradeDataset(series, entryIndexes, "точки входа");
        XYDataset exitDataset = createTradeDataset(series, exitIndexes, "точки выхода");


        XYPlot candlesAndMAPlot = new XYPlot(candleDataset, new DateAxis("Time"), new NumberAxis("Price"), null);
        candlesAndMAPlot.setRenderer(new CandlestickRenderer());
        setBackgroundColor(candlesAndMAPlot);

        XYPlot rsiPlot = new XYPlot();
        rsiPlot.setRangeAxis(new NumberAxis("Value"));
        rsiPlot.setDomainAxis(new DateAxis("Time"));
        setBackgroundColor(rsiPlot);


        //отображение свечного графика, скользящих средних и точек входа и выхода
        CandlestickRenderer renderer = new CandlestickRenderer();
        renderer.setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_AVERAGE);
        candlesAndMAPlot.setRenderer(renderer);

        candlesAndMAPlot.setDataset(1, shortEmaDataset);
        XYLineAndShapeRenderer shortEmaRenderer = new XYLineAndShapeRenderer(true, false);
        shortEmaRenderer.setSeriesPaint(0, Color.black);
        candlesAndMAPlot.setRenderer(1, shortEmaRenderer);

        candlesAndMAPlot.setDataset(2, longEmaDataset);
        XYLineAndShapeRenderer longEmaRenderer = new XYLineAndShapeRenderer(true, false);
        longEmaRenderer.setSeriesPaint(0, Color.CYAN);
        candlesAndMAPlot.setRenderer(2, longEmaRenderer);



        candlesAndMAPlot.setDataset(3, entryDataset);
        XYLineAndShapeRenderer entryRenderer = new XYLineAndShapeRenderer(false, true){
            @Override
            public Paint getItemPaint(int series, int item) {
                return new Color(0, 200, 0, 0);
            }

            @Override
            public Paint getItemOutlinePaint(int series, int item) {
                return new Color(0, 200, 0);
            }
        };
        entryRenderer.setSeriesShape(0, new Ellipse2D.Double(-7.5, -7.5, 15, 15));
        entryRenderer.setSeriesStroke(0, new BasicStroke(3));
        entryRenderer.setDrawOutlines(true);
        entryRenderer.setUseOutlinePaint(true);
        candlesAndMAPlot.setRenderer(3, entryRenderer);

        candlesAndMAPlot.setDataset(4, exitDataset);
        XYLineAndShapeRenderer exitRenderer = new XYLineAndShapeRenderer(false, true){
            @Override
            public Paint getItemPaint(int series, int item) {
                return new Color(255, 0, 0, 0);
            }

            @Override
            public Paint getItemOutlinePaint(int series, int item) {
                return new Color(255, 0, 0);
            }
        };;
        exitRenderer.setSeriesShape(0, new Ellipse2D.Double(-7.5, -7.5, 15, 15));
        exitRenderer.setSeriesStroke(0, new BasicStroke(2));
        exitRenderer.setDrawOutlines(true);
        exitRenderer.setUseOutlinePaint(true);
        candlesAndMAPlot.setRenderer(4, exitRenderer);


        //отображение индекса rsi
        rsiPlot.setDataset(0, rsiDataset);
        XYLineAndShapeRenderer rsiRenderer = new XYLineAndShapeRenderer(true, false);
        rsiRenderer.setSeriesPaint(0, Color.RED);
        rsiPlot.setRenderer(0, rsiRenderer);

        rsiPlot.setDataset(1, createRsiCheckPoints());
        XYLineAndShapeRenderer rsiEdgesRenderer = new XYLineAndShapeRenderer(true, false);
        rsiEdgesRenderer.setSeriesPaint(0, new Color(80, 0, 17));
        rsiEdgesRenderer.setSeriesPaint(1, new Color(40, 231, 0));
        rsiEdgesRenderer.setSeriesPaint(2, new Color(187, 0, 32));
        rsiPlot.setRenderer(1, rsiEdgesRenderer);

        XYPolygonAnnotation rect = createRsiArea();
        rsiPlot.addAnnotation(rect);



        ((NumberAxis) candlesAndMAPlot.getRangeAxis()).setAutoRangeIncludesZero(false);
        ((NumberAxis) rsiPlot.getRangeAxis()).setAutoRangeIncludesZero(false);

        CombinedDomainXYPlot combinedPlot = new CombinedDomainXYPlot(new NumberAxis("Стратегия с двумя скользящими средними и RSI"));
        ((NumberAxis) combinedPlot.getDomainAxis()).setAutoRangeIncludesZero(false);
        combinedPlot.setGap(10);
        combinedPlot.add(candlesAndMAPlot, 3);
        combinedPlot.add(rsiPlot, 1);

        //setBackgroundColor(combinedPlot);

        JFreeChart combinedChart = new JFreeChart("Combined chart", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);

        BufferedImage image = combinedChart.createBufferedImage(1500, 1000);
        chartOutput = new ByteArrayOutputStream();
        ImageIO.write(image, "png", chartOutput);
        chartOutput.close();
    }

    public ByteArrayOutputStream getMAStrategyPicture(String title,
                                     BarSeries series, TradingRecord record,
                                     Indicator<Num> shortIndicator, Indicator<Num> longIndicator,
                                                      Indicator<Num> rsiIndicator) throws IOException {
        visualizeMAStrategy(title, series, record, shortIndicator, longIndicator, rsiIndicator);

        return chartOutput;
    }

    private OHLCDataset createCandleDataset(BarSeries series){
        return new DefaultOHLCDataset(
                series.getName(),
                series.getBarData().stream()
                        .map(bar -> new OHLCDataItem(
                                Date.from(bar.getEndTime()),
                                bar.getOpenPrice().doubleValue(),
                                bar.getHighPrice().doubleValue(),
                                bar.getLowPrice().doubleValue(),
                                bar.getClosePrice().doubleValue(),
                                bar.getVolume().doubleValue()
                        )).toArray(OHLCDataItem[]::new)
        );
    }


    private XYDataset createIndicatorDataset(BarSeries series, Indicator<Num> indicator, String name, boolean normalize){
        XYSeries maSeries = new XYSeries(name);

        for(int i=0; i<series.getBarCount();i++){
            if(i >= indicator.getBarSeries().getBeginIndex()){
                var bar = series.getBar(i);
                Instant time = bar.getEndTime();
                long millis = time.toEpochMilli();


                maSeries.add(millis, indicator.getValue(i).doubleValue());

                if(i == 0 && startX == -1){
                    startX = millis;
                }else if(i == series.getBarCount() - 1 && endX == -1){
                    endX = millis;
                }
            }

        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(maSeries);
        return dataset;
    }


    private XYDataset createTradeDataset(BarSeries series, List<Integer> indexes, String name){
        XYSeries tradeSeries = new XYSeries(name);

        for (var index : indexes) {
            if (index >= 0 && index < series.getBarCount()) {
                var bar = series.getBar(index);
                Instant time = bar.getEndTime();
                long millis = time.toEpochMilli();

                tradeSeries.add(millis, bar.getClosePrice().doubleValue());
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(tradeSeries);
        return dataset;
    }

    private XYDataset createRsiCheckPoints(){
        XYSeries[] seriesArray = {
                new XYSeries("RSI 70", false),
                new XYSeries("RSI 50", false),
                new XYSeries("RSI 30", false)
        };

        seriesArray[0].add(startX, rsiCheckpoints[2]);
        seriesArray[0].add(endX, rsiCheckpoints[2]);
        seriesArray[1].add(startX, rsiCheckpoints[1]);
        seriesArray[1].add(endX, rsiCheckpoints[1]);
        seriesArray[2].add(startX, rsiCheckpoints[0]);
        seriesArray[2].add(endX, rsiCheckpoints[0]);


        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(seriesArray[0]);
        dataset.addSeries(seriesArray[1]);
        dataset.addSeries(seriesArray[2]);
        return dataset;
    }

    @NotNull
    private XYPolygonAnnotation createRsiArea() {
        double [] coords = {startX, rsiCheckpoints[0],
                endX, rsiCheckpoints[0],
                endX, rsiCheckpoints[2],
                startX, rsiCheckpoints[2]};
        return new XYPolygonAnnotation(
                coords,
                new BasicStroke(0.0f),
                new Color(100, 150, 200, 0),
                new Color(165, 223, 231, 66)
        );
    }

    private void setBackgroundColor(XYPlot plot){
        ZonedDateTime time = ZonedDateTime.now();
        int hour = time.getHour();

        if(hour >= 6 && hour <= 17){
            plot.setBackgroundPaint(new Color(255, 255, 255));
        }else{
            plot.setBackgroundPaint(new Color(61, 58, 58));
        }
    }
}
