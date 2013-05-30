package org.c_base.c_beam.ccorder;

import android.graphics.Color;
import android.hardware.SensorEvent;
import com.androidplot.Plot;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

import java.util.ArrayList;

public class SensorPlot {
    private final XYPlot plot;
    private SimpleXYSeries seriesX;
    private SimpleXYSeries seriesY;
    private SimpleXYSeries seriesZ;

    public SensorPlot(String sensorName, XYPlot plot) {
        LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.rgb(0, 0, 200), null, Color.rgb(0, 0, 80));
        series1Format.getFillPaint().setAlpha(150);
        LineAndPointFormatter series2Format = new LineAndPointFormatter(Color.rgb(0, 200, 0), null, Color.rgb(0, 80, 0));
        series2Format.getFillPaint().setAlpha(150);
        LineAndPointFormatter series3Format = new LineAndPointFormatter(Color.rgb(200, 0, 0), null, Color.rgb(80, 0, 0));
        series3Format.getFillPaint().setAlpha(150);

        this.plot = plot;
        plot.setTitle(sensorName);

        seriesX = new SimpleXYSeries(new ArrayList<Number>(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, sensorName + " X");
        seriesY = new SimpleXYSeries(new ArrayList<Number>(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, sensorName + " Y");
        seriesZ = new SimpleXYSeries(new ArrayList<Number>(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, sensorName + " Z");

        plot.addSeries(seriesX, series1Format);
        plot.addSeries(seriesY, series2Format);
        plot.addSeries(seriesZ, series3Format);
    }
    
    public void addEvent(SensorEvent event) {
        if (seriesX.size() > 100) {
            seriesX.removeFirst();
        }
        seriesX.addLast(null, event.values[0]);
        if (seriesY.size() > 100) {
            seriesY.removeFirst();
        }
        seriesY.addLast(null, event.values[1]);
        if (seriesZ.size() > 100) {
            seriesZ.removeFirst();
        }
        seriesZ.addLast(null, event.values[2]);
        plot.redraw();
    }
}