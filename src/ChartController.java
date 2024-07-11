import java.util.Date;
import java.util.Hashtable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.GridPane;

public class ChartController {
    @FXML private GridPane grid;
    String[] chartIds = {"maxVChart", "maxTChart", "sumVChart", "delVChart"};
    private Date startTime;
    Hashtable<String, LineChart.Series<Number, Number>> seriesList;
    private int numCell;

    public void initialize() {
        int lowerV = 2;
        int upperV = 4;
        double tickUnit = (upperV - lowerV) / 20;

        for (String chartId : chartIds) {
            @SuppressWarnings("unchecked")
            LineChart<Number, Number> chart = (LineChart<Number, Number>) grid.lookup("#" + chartId);

            NumberAxis xAxis = (NumberAxis) chart.getXAxis();
            NumberAxis yAxis = (NumberAxis) chart.getYAxis();

            xAxis.setAutoRanging(false);
            xAxis.setTickLabelsVisible(true);
            xAxis.setLabel("Time (s)");
            xAxis.setLowerBound(0);
            xAxis.setUpperBound(600);
            xAxis.setTickUnit(30);

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
        startTime = new Date();
    }

    public void initSeries(Hashtable<String, Double> maxmin) {
        for (String key : maxmin.keySet()) {
            seriesList.put(key, new LineChart.Series<Number, Number>());
        }

        for (String chartId : chartIds) {
            @SuppressWarnings("unchecked")
            LineChart<Number, Number> chart = (LineChart<Number, Number>) grid.lookup("#" + chartId);
            switch (chartId) {
                case "maxVChart":
                    chart.getData().add(seriesList.get("maxV"));
                    chart.getData().add(seriesList.get("minV"));
                    break;
                case "maxTChart":
                    chart.getData().add(seriesList.get("maxT"));
                    break;
                case "sumVChart":
                    chart.getData().add(seriesList.get("sumV"));

                    break;
                case "delVChart":
                    chart.getData().add(seriesList.get("delV"));
                    break;
                default:
                    break;
            }
        }
    }

    public void updateSeries(Hashtable<String, Double> maxmin, Date timestamp) {
        int secondsElapsed = (int) ((timestamp.getTime() - startTime.getTime()) / 1000);
        // System.out.println(secondsElapsed);
        for (String key : maxmin.keySet()) {
            Platform.runLater(() -> seriesList.get(key).getData().add(new LineChart.Data<Number, Number>(secondsElapsed, maxmin.get(key))));
        }
    }

    public void setNumCell(int numCell) {
        this.numCell = numCell;
    }
}
