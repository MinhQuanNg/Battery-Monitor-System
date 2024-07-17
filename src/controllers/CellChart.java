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
    private int xUpperBound = 5;
    private double xTickUnit = (double) xUpperBound / 10;
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
        chartV.getStyleClass().add("chart-voltage");
        chartT.getData().add(temperature);
        chartT.getStyleClass().add("chart-temperature");

        startTime = new Date();
    }

    public void updateSeries(double voltage, double temperature, Date timestamp) {
        int secondsElapsed = (int) ((timestamp.getTime() - startTime.getTime()) / 1000);

        LineChart.Series<Number, Number> voltageSeries = chartV.getData().get(0);
        LineChart.Series<Number, Number> temperatureSeries = chartT.getData().get(0);

        Platform.runLater(() -> {
            voltageSeries.getData().add(new LineChart.Data<Number, Number>(secondsElapsed, voltage));
            temperatureSeries.getData().add(new LineChart.Data<Number, Number>(secondsElapsed, temperature));

            if (secondsElapsed >= VxAxis.getUpperBound()) {
                VxAxis.setLowerBound(VxAxis.getLowerBound() + xTickUnit);
                VxAxis.setUpperBound(VxAxis.getUpperBound() + xTickUnit);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void scroll(ScrollEvent e) {
        LineChart<Number, Number> chart = (LineChart<Number, Number>) e.getSource();
        NumberAxis xAxis = (NumberAxis) chart.getXAxis();

        // On scroll left, move the x-axis to the left by xTickUnit
        if (e.getDeltaX() > 0) {
            double newLowerBound = xAxis.getLowerBound() - xTickUnit;
            double newUpperBound = xAxis.getUpperBound() - xTickUnit;
            xAxis.setLowerBound(Math.max(0, newLowerBound)); // Prevent going below 0
            xAxis.setUpperBound(Math.max(xUpperBound, newUpperBound));
        }

        // On scroll right, only shift if there is data to show
        else if (xAxis.getUpperBound() <= secondsElapsed) {
            double newLowerBound = xAxis.getLowerBound() + xTickUnit;
            double newUpperBound = xAxis.getUpperBound() + xTickUnit;
            xAxis.setLowerBound(newLowerBound);
            xAxis.setUpperBound(newUpperBound);
        }
    }
}