package test;
import java.util.Hashtable;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

public class ChartCtrlTest {
    @FXML private LineChart<Number, Number> chart;
    @FXML private NumberAxis xAxis, yAxis;
    private LineChart.Series<Number, Number> series1, series2;
    
    @SuppressWarnings("unchecked")
    private void test() throws InterruptedException {
        Hashtable<String, Double> test = new Hashtable<>();
        
        test.put("maxV", 10.5);
        dataToSeries(series1, test, 1);

        test.put("maxV", 11.5);
        dataToSeries(series1, test, 2);

        test.put("maxV", 10.5);
        dataToSeries(series1, test, 3);
        chart.getData().add(series1);

        test.put("maxV", 2.5);
        dataToSeries(series2, test, 1);

        test.put("maxV", 3.0);
        dataToSeries(series2, test, 2);

        test.put("maxV", 4.9);
        dataToSeries(series2, test, 3);
        chart.getData().add(series2);

        series1.getNode().getStyleClass().add("series1");
        series2.getNode().getStyleClass().add("series2");

        // series.getNode().setStyle("-fx-stroke: blue; -fx-background-color: blue;");

        // Thread.sleep(1000);

        // test.put("maxV", 5.0);
        // dataToSeries(series, test, 4);

        // Thread.sleep(1000);
        // test.put("maxV", 6.0);
        // dataToSeries(series, test, 7);
    }

    public void initialize() {
        xAxis.setAutoRanging(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setMinorTickVisible(false);

        series1 = new LineChart.Series<Number, Number>();
        series2 = new LineChart.Series<Number, Number>();
        series1.setName("maxV");
        series2.setName("minV");

        chart.setAnimated(false);

        try {
            test();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void dataToSeries(LineChart.Series<Number, Number> series, Hashtable<String, Double> maxmin, int secondsElapsed) {
        series.getData().add(new LineChart.Data<Number, Number>(secondsElapsed, maxmin.get("maxV")));
        System.out.println(series.getData().getLast().getYValue());
    }
}
