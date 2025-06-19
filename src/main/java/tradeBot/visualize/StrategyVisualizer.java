package tradeBot.visualize;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPolygonAnnotation;
import org.jfree.chart.axis.NumberAxis;
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

    private final double[] rsiCheckpoints = {30, 50, 70};


    public void visualizeMAStrategy(
            String title,
            BarSeries series, TradingRecord record,
            Indicator<Num> shortIndicator, Indicator<Num> longIndicator,
            Indicator<Num> rsiIndicator
    ) throws IOException {
        OHLCDataset candleDataset = createCandleDataset(series);

        XYDataset shortEmaDataset = createIndicatorDataset(series, shortIndicator, "Short MA", false);
        XYDataset longEmaDataset = createIndicatorDataset(series, longIndicator, "Long MA", false);

        XYDataset rsiDataset = createIndicatorDataset(series, rsiIndicator, "RSI", true);

        List<Integer> entryIndexes = new ArrayList<>();
        List<Integer> exitIndexes = new ArrayList<>();

        series.getBarData().forEach(e -> {
            double closePrice = e.getClosePrice().doubleValue();
            if(closePrice > maxPrice) maxPrice = closePrice;
        });


        for(var position: record.getPositions()){
            entryIndexes.add(position.getEntry().getIndex());
            exitIndexes.add(position.getExit().getIndex());
        }


        XYDataset entryDataset = createTradeDataset(series, entryIndexes, "точки входа");
        XYDataset exitDataset = createTradeDataset(series, exitIndexes, "точки выхода");

        JFreeChart chart = ChartFactory.createCandlestickChart(title, "time", "Price",
                candleDataset, true);

        XYPlot plot = chart.getXYPlot();

        CandlestickRenderer renderer = new CandlestickRenderer();
        renderer.setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_AVERAGE);
        plot.setRenderer(renderer);


        plot.setDataset(1, shortEmaDataset);
        XYLineAndShapeRenderer shortEmaRenderer = new XYLineAndShapeRenderer(true, false);
        shortEmaRenderer.setSeriesPaint(0, Color.black);
        plot.setRenderer(1, shortEmaRenderer);

        plot.setDataset(2, longEmaDataset);
        XYLineAndShapeRenderer longEmaRenderer = new XYLineAndShapeRenderer(true, false);
        longEmaRenderer.setSeriesPaint(0, Color.CYAN);
        plot.setRenderer(2, longEmaRenderer);


        plot.setDataset(3, rsiDataset);
        XYLineAndShapeRenderer rsiRenderer = new XYLineAndShapeRenderer(true, false);
        rsiRenderer.setSeriesPaint(0, Color.RED);
        plot.setRenderer(3, rsiRenderer);

        plot.setDataset(4, createRsiCheckPoints());
        XYLineAndShapeRenderer rsiEdgesRenderer = new XYLineAndShapeRenderer(true, false);
        rsiEdgesRenderer.setSeriesPaint(0, Color.RED);
        rsiEdgesRenderer.setSeriesPaint(1, Color.RED);
        rsiEdgesRenderer.setSeriesPaint(2, Color.RED);
        plot.setRenderer(4, rsiEdgesRenderer);

        XYPolygonAnnotation rect = createRsiArea();
        plot.addAnnotation(rect);


        plot.setDataset(5, entryDataset);
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
        entryRenderer.setSeriesShape(0, new Ellipse2D.Double(-12.5, -12.5, 25, 25));
        entryRenderer.setSeriesStroke(0, new BasicStroke(3));
        entryRenderer.setDrawOutlines(true);
        entryRenderer.setUseOutlinePaint(true);
        plot.setRenderer(5, entryRenderer);

        plot.setDataset(6, exitDataset);
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
        exitRenderer.setSeriesShape(0, new Ellipse2D.Double(-12.5, -12.5, 25, 25));
        exitRenderer.setSeriesStroke(0, new BasicStroke(2));
        exitRenderer.setDrawOutlines(true);
        exitRenderer.setUseOutlinePaint(true);
        plot.setRenderer(6, exitRenderer);


        NumberAxis numberAxis = (NumberAxis) plot.getRangeAxis();
        numberAxis.setAutoRangeIncludesZero(false);

        BufferedImage image = chart.createBufferedImage(1000, 500);
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
                new Color(100, 150, 200),
                new Color(166, 241, 219, 100)
        );
    }
}
