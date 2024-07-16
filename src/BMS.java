import constants.Style;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class BMS extends Application {
    @Override
    public void start(Stage stage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("./resources/ScreenMain.fxml"));            
            Scene scene = new Scene(root, Style.WIDTH, Style.HEIGHT);
            stage.setScene(scene);
            stage.setTitle("Hệ thống quản lý pin");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}