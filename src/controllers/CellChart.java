package controllers;

import java.util.Date;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

public class CellChart {
    private int cell;
    @FXML private LineChart<Number, Number> chart;
    @FXML private NumberAxis xAxis, vAxis, tAxis;
    private Date startTime;

    public CellChart(int cell) {
        this.cell = cell;
    }

    public void initialize() {
        chart.setTitle("Cell " + cell);

        xAxis.setAutoRanging(false);
        xAxis.setTickLabelsVisible(true);
        xAxis.setLabel("Time (s)");
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(600);
        xAxis.setTickUnit(30);

        vAxis.setAutoRanging(false);
        vAxis.setTickLabelsVisible(true);
        vAxis.setLabel("Voltage (V)");
        vAxis.setLowerBound(2);
        vAxis.setUpperBound(4);
        vAxis.setTickUnit(0.1);

        tAxis.setAutoRanging(false);
        tAxis.setTickLabelsVisible(true);
        tAxis.setLabel("Temperature (°C)");
        tAxis.setLowerBound(2);
        tAxis.setUpperBound(4);
        tAxis.setTickUnit(0.1);

        LineChart.Series<Number, Number> voltage = new LineChart.Series<Number, Number>();
        LineChart.Series<Number, Number> temperature = new LineChart.Series<Number, Number>();

        voltage.setName("Voltage");
        temperature.setName("Temperature");

        chart.getData().add(voltage);
        chart.getData().add(temperature);

        startTime = new Date();
    }

    public void updateSeries(double voltage, double temperature, Date timestamp) {
        int secondsElapsed = (int) ((timestamp.getTime() - startTime.getTime()) / 1000);

        LineChart.Series<Number, Number> voltageSeries = chart.getData().get(0);
        LineChart.Series<Number, Number> temperatureSeries = chart.getData().get(1);

        voltageSeries.getData().add(new LineChart.Data<Number, Number>(secondsElapsed, voltage));
        temperatureSeries.getData().add(new LineChart.Data<Number, Number>(secondsElapsed, temperature));
    }
}