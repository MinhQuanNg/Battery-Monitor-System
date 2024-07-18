package controllers;

import java.util.Date;
import java.util.Hashtable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;

public class Charts {
    @FXML private GridPane grid;
    private String[] chartIds = {"maxVChart", "maxTChart", "sumVChart", "delVChart"};
    private Date startTime;
    private Hashtable<String, LineChart.Series<Number, Number>> seriesList;
    private int numCell;

    private int xUpperBound = 600;
    private double xTickUnit = (double) xUpperBound / 20;
    private int secondsElapsed;

    public Charts(int numCell, Date startime) {
        this.numCell = numCell;
        this.startTime = startime;
    }

    public void initialize() {
        int lowerV = 2;
        int upperV = 4;
        double tickUnit = (double) (upperV - lowerV) / 20;

        for (String chartId : chartIds) {
            @SuppressWarnings("unchecked")
            LineChart<Number, Number> chart = (LineChart<Number, Number>) grid.lookup("#" + chartId);

            NumberAxis xAxis = (NumberAxis) chart.getXAxis();
            NumberAxis yAxis = (NumberAxis) chart.getYAxis();

            xAxis.setAutoRanging(false);
            xAxis.setTickLabelsVisible(true);
            xAxis.setLabel("Time (s)");
            xAxis.setLowerBound(0);
            xAxis.setUpperBound(xUpperBound);
            xAxis.setTickUnit(xTickUnit);

            yAxis.setAutoRanging(false);
            yAxis.setTickLabelsVisible(true);

            switch (chartId) {
                case "maxVChart":
                    chart.setTitle("Max and Min Voltage");
                    yAxis.setLabel("Voltage (V)");
                    
                    yAxis.setLowerBound(lowerV);
                    yAxis.setUpperBound(upperV);
                    yAxis.setTickUnit(tickUnit);
                    break;
                case "maxTChart":
                    chart.setTitle("Max Temperature");
                    yAxis.setLabel("Temperature (°C)");

                    yAxis.setLowerBound(0);
                    yAxis.setUpperBound(100);
                    yAxis.setTickUnit(5);
                    break;
                case "sumVChart":
                    chart.setTitle("Sum Voltage");
                    yAxis.setLabel("Voltage (V)");

                    yAxis.setLowerBound(lowerV * numCell);
                    yAxis.setUpperBound(upperV * numCell);
                    yAxis.setTickUnit(tickUnit * numCell);
                    break;
                case "delVChart":
                    chart.setTitle("Delta Voltage");
                    yAxis.setLabel("Voltage (V)");

                    yAxis.setLowerBound(0);
                    yAxis.setUpperBound(0.4);
                    yAxis.setTickUnit(0.02);
                    break;
                default:
                    break;
            }
        }

        seriesList = new Hashtable<>();
    }

    // Initialize data series for each chart
    public void initSeries(Hashtable<String, Double> maxmin) {
        for (String key : maxmin.keySet()) {
            if (!key.equals("sumT")) {
                seriesList.put(key, new LineChart.Series<Number, Number>());
            }
        }

        Platform.runLater(() -> {
            for (String chartId : chartIds) {
                @SuppressWarnings("unchecked")
                LineChart<Number, Number> chart = (LineChart<Number, Number>) grid.lookup("#" + chartId);
                switch (chartId) {
                    case "maxVChart":
                        seriesList.get("maxV").setName("Max V");
                        chart.getData().add(seriesList.get("maxV"));

                        seriesList.get("minV").setName("Min V");
                        chart.getData().add(seriesList.get("minV"));

                        chart.getStyleClass().add("maxV-chart");
                        break;
                    case "maxTChart":
                        seriesList.get("maxT").setName("Max T");
                        chart.getData().add(seriesList.get("maxT"));

                        chart.getStyleClass().add("maxT-chart");
                        break;
                    case "sumVChart":
                        seriesList.get("sumV").setName("Sum V");
                        chart.getData().add(seriesList.get("sumV"));

                        chart.getStyleClass().add("sumV-chart");
                        break;
                    case "delVChart":
                        seriesList.get("delV").setName("Delta V");
                        chart.getData().add(seriesList.get("delV"));

                        chart.getStyleClass().add("delV-chart");
                        break;
                    default:
                        break;
                }
            }
        });
    }

    // Update chart with new data
    public void updateSeries(Hashtable<String, Double> maxmin, Date timestamp) {
        secondsElapsed = (int) ((timestamp.getTime() - startTime.getTime()) / 1000);

        for (String key : maxmin.keySet()) {
            if (!key.equals("sumT")) {
                Platform.runLater(() -> {
                    LineChart.Series<Number, Number> series = seriesList.get(key);
                    series.getData().add(new LineChart.Data<Number, Number>(secondsElapsed, maxmin.get(key)));
                    
                    // Series might not have been added to chart yet 
                    if (series.getChart() != null) {
                        NumberAxis xAxis = (NumberAxis) series.getChart().getXAxis();
                    
                        // Shift the chart to the right if the x-axis is at the end if the chart has not been scrolled
                        if (secondsElapsed >= xAxis.getUpperBound()) {
                            xAxis.setLowerBound(xAxis.getLowerBound() + xTickUnit);
                            xAxis.setUpperBound(xAxis.getUpperBound() + xTickUnit);
                        }
                    }
                });
            }
        }
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
