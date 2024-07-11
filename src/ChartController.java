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
        // xAxis.setAutoRanging(false);
        // xAxis.setTickLabelsVisible(false);
        // xAxis.setTickMarkVisible(false);
        // xAxis.setMinorTickVisible(false);

        series = new LineChart.Series<Number, Number>();
        series.setName("maxV"); 

        startChart = new Date();

        chart.setAnimated(false);
        chart.getData().addAll(series);
    }

    public void dataToSeries(Hashtable<String, Double> maxmin, Date timestamp) {
        int secondsElapsed = (int) ((timestamp.getTime() - startChart.getTime()) / 1000);
        System.out.println(secondsElapsed);
        Platform.runLater(() -> series.getData().add(new LineChart.Data<Number, Number>(secondsElapsed, maxmin.get("maxV"))));
    }
}
