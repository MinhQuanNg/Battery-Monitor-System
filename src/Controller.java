import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;

public class Controller {
    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML private Label maxTempLabel;

    public void thoigianthuc(ActionEvent e) throws IOException {
        root = FXMLLoader.load(getClass().getResource("ScreenGeneral.fxml"));
        stage = (Stage)((Node) e.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void updateDisplay(String maxTemp) {
        maxTempLabel.setText(maxTemp);
    }
}
