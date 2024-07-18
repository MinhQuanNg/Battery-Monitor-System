package controllers;

import java.util.Date;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.input.ScrollEvent;

public class CellChart {
    private int cell;
    @FXML private LineChart<Number, Number> chartV, chartT;
    @FXML private NumberAxis VxAxis, TxAxis, vAxis, tAxis;
    private Date startTime;
    private int xUpperBound = 600;
    private double xTickUnit = (double) xUpperBound / 20;
    private double secondsElapsed;

    public CellChart(int cell) {
        this.cell = cell;
    }

    public void initialize() {
        chartV.setTitle("Cell " + cell);
        chartT.setTitle("Cell " + cell); // this will be invisible, it's only for aligning the charts

        VxAxis.setAutoRanging(false);
        VxAxis.setTickLabelsVisible(true);
        VxAxis.setLabel("Time (s)");
        VxAxis.setLowerBound(0);
        VxAxis.setUpperBound(xUpperBound);
        VxAxis.setTickUnit(xTickUnit);
        
        // invisible, setting these for alignment
        TxAxis.setAutoRanging(false);
        TxAxis.setLabel("...");
        TxAxis.setLowerBound(0);
        TxAxis.setUpperBound(xUpperBound);

        vAxis.setAutoRanging(false);
        vAxis.setTickLabelsVisible(true);
        vAxis.setLabel("Voltage (V)");
        vAxis.setLowerBound(2);
        vAxis.setUpperBound(4);
        vAxis.setTickUnit(0.1);

        tAxis.setAutoRanging(false);
        tAxis.setTickLabelsVisible(true);
        tAxis.setLabel("Temperature (°C)");
        tAxis.setLowerBound(0);
        tAxis.setUpperBound(100);
        tAxis.setTickUnit(5);

        LineChart.Series<Number, Number> voltage = new LineChart.Series<Number, Number>();
        LineChart.Series<Number, Number> temperature = new LineChart.Series<Number, Number>();

        voltage.setName("Voltage");
        temperature.setName("Temperature");

        chartV.getData().add(voltage);
        chartT.getData().add(temperature);

        startTime = new Date();
    }

    public void updateSeries(double voltage, double temperature, Date timestamp) {
        secondsElapsed = (int) ((timestamp.getTime() - startTime.getTime()) / 1000);

        LineChart.Series<Number, Number> voltageSeries = chartV.getData().get(0);
        LineChart.Series<Number, Number> temperatureSeries = chartT.getData().get(0);

        Platform.runLater(() -> {
            voltageSeries.getData().add(new LineChart.Data<Number, Number>(secondsElapsed, voltage));
            temperatureSeries.getData().add(new LineChart.Data<Number, Number>(secondsElapsed, temperature));

            if (secondsElapsed >= VxAxis.getUpperBound()) {
                VxAxis.setLowerBound(VxAxis.getLowerBound() + xTickUnit);
                VxAxis.setUpperBound(VxAxis.getUpperBound() + xTickUnit);
                TxAxis.setLowerBound(TxAxis.getLowerBound() + xTickUnit);
                TxAxis.setUpperBound(TxAxis.getUpperBound() + xTickUnit);
            }
        });
    }

    public void scroll(ScrollEvent e) {
        // On scroll left, move the x-axis to the left by xTickUnit
        if (e.getDeltaX() > 0) {
            double newLowerBound = VxAxis.getLowerBound() - xTickUnit;
            double newUpperBound = VxAxis.getUpperBound() - xTickUnit;
            VxAxis.setLowerBound(Math.max(0, newLowerBound)); // Prevent going below 0
            VxAxis.setUpperBound(Math.max(xUpperBound, newUpperBound));
            TxAxis.setLowerBound(Math.max(0, newLowerBound));
            TxAxis.setUpperBound(Math.max(xUpperBound, newUpperBound));
        }

        // On scroll right, only shift if there is data to show
        else if (VxAxis.getUpperBound() <= secondsElapsed) {
            double newLowerBound = TxAxis.getLowerBound() + xTickUnit;
            double newUpperBound = TxAxis.getUpperBound() + xTickUnit;
            VxAxis.setLowerBound(newLowerBound);
            VxAxis.setUpperBound(newUpperBound);
            TxAxis.setLowerBound(newLowerBound);
            TxAxis.setUpperBound(newUpperBound);
        }
    }
}