package controllers;

import java.util.Date;

import constants.Style;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

public class CellChart {
    private int cell;
    @FXML private LineChart<Number, Number> chartV, chartT;
    @FXML private NumberAxis VxAxis, TxAxis, vAxis, tAxis;
    private Date startTime;

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
        VxAxis.setUpperBound(600);
        VxAxis.setTickUnit(30);
        
        TxAxis.setAutoRanging(false);
        TxAxis.setTickLabelsVisible(true);
        TxAxis.setLabel("Time (s)");
        TxAxis.setLowerBound(0);
        TxAxis.setUpperBound(600);
        TxAxis.setTickUnit(30);

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
        int secondsElapsed = (int) ((timestamp.getTime() - startTime.getTime()) / 1000);

        LineChart.Series<Number, Number> voltageSeries = chartV.getData().get(0);
        LineChart.Series<Number, Number> temperatureSeries = chartT.getData().get(0);

        Platform.runLater(() -> {
            voltageSeries.getData().add(new LineChart.Data<Number, Number>(secondsElapsed, voltage));
            temperatureSeries.getData().add(new LineChart.Data<Number, Number>(secondsElapsed, temperature));
        });
    }
}