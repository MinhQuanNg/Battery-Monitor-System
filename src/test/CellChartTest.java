package test;

import controllers.CellChart;
import controllers.Charts;

import java.util.Date;
import java.util.Hashtable;

import constants.Style;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CellChartTest extends Application {
    @Override
    public void start(Stage stage) {
        try {
            // CellChart cellChart = new CellChart(1);
            // FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/CellChart.fxml"));
            // loader.setController(cellChart);

            Hashtable<String, Double> data = new Hashtable<>();
            data.put("maxV", 3.3);
            data.put("minV", 2.4);
            data.put("sumV", 96.0);
            data.put("maxT", 32.1);
            data.put("delV", 0.07);

            Charts charts = new Charts(1, new Date());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/Charts.fxml"));
            loader.setController(charts);
            Parent root = loader.load();
            charts.initSeries(data);
            Scene chartScene = new Scene(root, Style.CHART_WIDTH, Style.CHART_HEIGHT);

            Stage chartStage = new Stage();
            chartStage.setScene(chartScene);
            chartStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
