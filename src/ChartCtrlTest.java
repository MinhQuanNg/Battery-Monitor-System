import java.util.Hashtable;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

public class ChartCtrlTest {
    
    @FXML private LineChart<Number, Number> chart;
    @FXML private NumberAxis xAxis, yAxis;
    private LineChart.Series<Number, Number> series;
    
    @SuppressWarnings("unchecked")
    private void test() throws InterruptedException {
        Hashtable<String, Double> test = new Hashtable<>();
        
        test.put("maxV", 10.5);
        dataToSeries(series, test, 1);

        test.put("maxV", 11.5);
        dataToSeries(series, test, 2);

        test.put("maxV", 10.5);
        dataToSeries(series, test, 3);
        chart.getData().addAll(series);

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

        series = new LineChart.Series<Number, Number>();
        series.setName("maxV"); 

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
