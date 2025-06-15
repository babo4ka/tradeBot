package tradeBot.visualize;

import lombok.Getter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
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

    public void visualizeMAStrategy(
            String title,
            BarSeries series, TradingRecord record,
            Indicator<Num> shortIndicator, Indicator<Num> longIndicator
    ) throws IOException {
        OHLCDataset candleDataset = createCandleDataset(series);

        XYDataset shortEmaDataset = createMADataset(series, shortIndicator, "Short MA");
        XYDataset longEmaDataset = createMADataset(series, longIndicator, "Long MA");

        List<Integer> entryIndexes = new ArrayList<>();
        List<Integer> exitIndexes = new ArrayList<>();


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


        plot.setDataset(3, entryDataset);
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
        plot.setRenderer(3, entryRenderer);

        plot.setDataset(4, exitDataset);
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
        plot.setRenderer(4, exitRenderer);


        NumberAxis numberAxis = (NumberAxis) plot.getRangeAxis();
        numberAxis.setAutoRangeIncludesZero(false);

        BufferedImage image = chart.createBufferedImage(1000, 500);
        chartOutput = new ByteArrayOutputStream();
        ImageIO.write(image, "png", chartOutput);
        chartOutput.close();
    }


    public ByteArrayOutputStream getMAStrategyPicture(String title,
                                     BarSeries series, TradingRecord record,
                                     Indicator<Num> shortIndicator, Indicator<Num> longIndicator) throws IOException {
        visualizeMAStrategy(title, series, record, shortIndicator, longIndicator);

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


    private XYDataset createMADataset(BarSeries series, Indicator<Num> indicator, String name){
        XYSeries maSeries = new XYSeries(name);

        for(int i=0; i<series.getBarCount();i++){
            if(i >= indicator.getBarSeries().getBeginIndex()){
                var bar = series.getBar(i);
                Instant time = bar.getEndTime();
                long millis = time.toEpochMilli();


                maSeries.add(millis, indicator.getValue(i).doubleValue());
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(maSeries);
        return dataset;
    }


    private XYDataset createTradeDataset(BarSeries series, List<Integer> indexes, String name){
        XYSeries tradeSeries = new XYSeries(name);

        for(var index: indexes){
            if(index >= 0 && index < series.getBarCount()){
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

}
