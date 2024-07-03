import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import com.fazecast.jSerialComm.SerialPort;

public class Controller {
    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML private Label maxTempLabel;
    @FXML private GridPane cellPane;
    static SerialPort USB;

    public void thoigianthuc(ActionEvent e) throws IOException {
        // Check for available USB ports
        USB = PortChecker.getPort();

        // Run if port available
        if (USB != null) {
            root = FXMLLoader.load(getClass().getResource("ScreenGeneral.fxml"));            
            stage = (Stage)((Node) e.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Quản lý hệ thống pin");
            stage.show();
        } else {
            System.out.println("No USB Serial Ports found.");
        }
    }

    public static SerialPort getUSB() {
        return USB;
    }
}
