import javafx.application.Platform;
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
    @FXML private Label maxTempLabel;
    @FXML private GridPane cellPane;
    private SerialPort USB;
    Thread thread;

    public void enter(ActionEvent e) throws IOException {
        // Check for available USB ports
        USB = PortChecker.getPort();

        // Run if port available

        // debug
        // if (USB != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ScreenGeneral.fxml"));
            loader.setControllerFactory(param -> new ControllerGeneral(this)); // Pass this controller
            Parent root = loader.load();   
                   
            stage = (Stage)((Node) e.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);

            // Set on close request handler to exit the application
            stage.setOnCloseRequest(event -> {
                System.out.println("Exiting application...");
                thread.interrupt();
                Platform.exit();
            });

            stage.setTitle("Hệ thống quản lý pin");
            stage.show();

            
        // } else {
        //     System.out.println("No USB Serial Ports found.");
        // }
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public SerialPort getUSB() {
        return USB;
    }
}