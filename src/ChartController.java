import java.util.Date;
import java.util.Hashtable;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

public class ChartController {
    @FXML private LineChart<Number, Number> chart;
    @FXML private NumberAxis xAxis, yAxis;
    private LineChart.Series<Number, Number> series;
    private Date startChart;

    @SuppressWarnings("unchecked")
    public void initialize() {
        xAxis.setAutoRanging(false);
        xAxis.setTickLabelsVisible(true);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(600);
        xAxis.setTickUnit(30);

        yAxis.setAutoRanging(false);
        yAxis.setTickLabelsVisible(true);
        yAxis.setLowerBound(2);
        yAxis.setUpperBound(4);
        yAxis.setTickUnit(0.1);

        series = new LineChart.Series<Number, Number>();
        series.setName("maxV"); 

        startChart = new Date();

        chart.getData().addAll(series);
    }

    public void dataToSeries(Hashtable<String, Double> maxmin, Date timestamp) {
        int secondsElapsed = (int) ((timestamp.getTime() - startChart.getTime()) / 1000);
        // System.out.println(secondsElapsed);
        Platform.runLater(() -> series.getData().add(new LineChart.Data<Number, Number>(secondsElapsed, maxmin.get("maxV"))));
    }
}
