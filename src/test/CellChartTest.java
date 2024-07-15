package test;

import controllers.CellChart;


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
            CellChart cellChart = new CellChart(1);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/CellChart.fxml"));
            loader.setController(cellChart);
            Parent root = loader.load();
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
