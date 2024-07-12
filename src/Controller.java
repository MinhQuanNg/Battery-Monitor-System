import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import java.io.InputStream;
import javafx.scene.control.Label;
import java.util.List;

import com.fazecast.jSerialComm.SerialPort;

public class Controller {
    private Stage stage;
    private Scene scene;
    @FXML private GridPane cellPane;
    @FXML private ComboBox portBox;
    @FXML private Label stat;
    private SerialPort port;
    private List<SerialPort> availablePorts;

    @SuppressWarnings("unchecked")
    public void initialize() {
        availablePorts = PortChecker.getPorts();
        portBox.setItems(FXCollections.observableArrayList(availablePorts.stream()
            .map(SerialPort::getDescriptivePortName)
            .toArray()));
            portBox.getStyleClass().add("combo-box");
            if (!availablePorts.isEmpty()) {
                portBox.setPromptText("Chọn cổng kết nối");
                portBox.getSelectionModel().select(0); // Select the first port by default
                stat.setText("ĐANG KẾT NỐI " + portBox.getSelectionModel().getSelectedItem().toString());
                port = availablePorts.get(0); // Set the default port
            } else {
                portBox.setPromptText("Không có cổng kết nối");
            }
    }

    public void selectPort(ActionEvent e) {
        int i = portBox.getSelectionModel().getSelectedIndex();
            port = availablePorts.get(i);
            stat.setText("ĐANG KẾT NỐI " + port.getDescriptivePortName());
    }

    public void enter(ActionEvent e) throws IOException {
        try {
            System.out.println(port.getDescriptivePortName());
        } catch (NullPointerException e1) {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Cảnh báo");
                alert.setHeaderText("Không tìm thấy cổng");
                alert.setContentText("Vui lòng chọn cổng và thử lại.");
                alert.getButtonTypes().setAll(new ButtonType("Thử Lại", ButtonData.CANCEL_CLOSE));
                alert.showAndWait();
            });
        }
        // Run if port available
        // debug
        try (InputStream inputStream = PortChecker.preparePort(port)) {
            System.out.println("Port is ready.");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ScreenGeneral.fxml"));
            Parent root = loader.load();
            
            ControllerGeneral ctrlGen = loader.getController();
            DataReader reader = new DataReader(ctrlGen, inputStream);

            // Start thread to read data
            Thread thread = new Thread(reader);
            thread.start();

            ctrlGen.setPort(port);

            stage = (Stage)((Node) e.getSource()).getScene().getWindow();
            scene = new Scene(root, 800, 600);
            stage.setScene(scene);

            // Set on close request handler to exit the application
            stage.setOnCloseRequest(event -> {
                port.closePort();
                System.out.println("Exiting application...");
                // thread.interrupt();
                Platform.exit();
            });

            stage.setTitle("Hệ thống quản lý pin");
            stage.show();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
<<<<<<< HEAD
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Lỗi kết nối");
                alert.setHeaderText("Vui lòng kiểm tra kết nối và thử lại.");
                alert.setContentText("Không thể kết nối với " + port.getDescriptivePortName());
                alert.getButtonTypes().setAll(new ButtonType("Thử Lại", ButtonData.CANCEL_CLOSE));
                alert.showAndWait();
            });
        } 
=======
            // TODO: modal error message
        }
>>>>>>> c3f20a7 (add eng support)
    }

    public SerialPort getPort() {
        return port;
    }
}