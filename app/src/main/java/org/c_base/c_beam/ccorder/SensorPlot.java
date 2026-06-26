package org.c_base.c_beam.ccorder;

import android.graphics.Color;
import android.hardware.SensorEvent;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

import java.util.ArrayList;

public class SensorPlot {

    private final SimpleXYSeries[] series;

    public SensorPlot(String sensorName, String[] dimensions, XYPlot plot) {
        series = new SimpleXYSeries[dimensions.length];

        int[][] formatColors = {
                {Color.rgb(0, 0, 0), Color.RED},
                {Color.rgb(0, 0, 0), Color.GREEN},
                {Color.rgb(0, 0, 0), Color.BLUE},
                {Color.rgb(0, 0, 0), Color.YELLOW},
                {Color.rgb(0, 0, 0), Color.CYAN},
                {Color.rgb(0, 0, 0), Color.MAGENTA},
        };

        for (int i = 0; i < dimensions.length; i++) {
            series[i] = new SimpleXYSeries(new ArrayList<>(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, sensorName + " " + dimensions[i]);
            for (int j = 0; j < 100; j++) {
                series[i].addLast(null, 0);
            }
            int[] colors = formatColors[i % formatColors.length];
            plot.addSeries(series[i], new LineAndPointFormatter(colors[1], null, colors[0], null));
        }

        plot.setTicksPerRangeLabel(3);
        plot.getGraphWidget().setDomainLabelOrientation(-45);
    }

    public void addEvent(SensorEvent event) {
        for (int i = 0; i < series.length; i++) {
            series[i].removeFirst();
            series[i].addLast(null, event.values[i]);
        }
    }
}
