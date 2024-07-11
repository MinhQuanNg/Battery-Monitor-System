import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChartTest extends Application {

    @Override
    public void start(Stage stage) {
        Scene chartScene;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LineChart.fxml"));
        try {
            loader.setController(new ChartCtrlTest());
            Parent root = loader.load();

            chartScene = new Scene(root, 600, 400);
            stage.setScene(chartScene);
        } catch (IOException e) {
            e.printStackTrace();
        }

        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
