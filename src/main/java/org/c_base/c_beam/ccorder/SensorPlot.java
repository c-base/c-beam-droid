package org.c_base.c_beam.ccorder;

import android.graphics.Color;
import android.hardware.SensorEvent;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import java.util.ArrayList;

public class SensorPlot {
    private final XYPlot plot;
    private SimpleXYSeries[] series;

    private SimpleXYSeries seriesX;
    private SimpleXYSeries seriesY;
    private SimpleXYSeries seriesZ;

    private int[][] formatColors = {
            {Color.rgb(0, 0, 200), Color.rgb(0, 0, 80)},
            {Color.rgb(0, 200, 0), Color.rgb(0, 80, 0)},
            {Color.rgb(200, 0, 0), Color.rgb(80, 0, 0)},
            {Color.rgb(200, 0, 200), Color.rgb(80, 0, 80)},
            {Color.rgb(0, 200, 200), Color.rgb(0, 80, 80)},
            {Color.rgb(200, 200, 0), Color.rgb(80, 80, 0)},
            {Color.rgb(200, 200, 200), Color.rgb(80, 80, 80)}
    };

    public SensorPlot(String sensorName, String[] dimensions, XYPlot plot) {
        this.plot = plot;
        plot.setTitle(sensorName);

        series = new SimpleXYSeries[dimensions.length];
        for (int i=0; i<dimensions.length; i++) {
            series[i] = new SimpleXYSeries(new ArrayList<Number>(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, sensorName + " " + dimensions[i]);
            for (int j=0; j<100; j++) {
                series[i].addLast(null, 0);
            }
            LineAndPointFormatter format = new LineAndPointFormatter(formatColors[i][0], null, formatColors[i][1], new PointLabelFormatter());
            format.getFillPaint().setAlpha(150);
            plot.addSeries(series[i], format);
        }
    }

    public void addEvent(SensorEvent event) {
        for (int i=0; i<series.length; i++) {
//            if (series[i].size() > 100) {
//                series[i].removeFirst();
//            }
            series[i].removeFirst();
            series[i].addLast(null, event.values[i]);
        }
        plot.redraw();
    }
}