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
    @FXML
    private GridPane cellPane;
    @SuppressWarnings("rawtypes")
    @FXML
    private ComboBox portBox;
    @FXML
    private Label stat;
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
        if (i >= 0 && i < availablePorts.size()) { // Ensure the index is valid
            port = availablePorts.get(i);
            if (port != null) { // Check if port is not null
                stat.setText("ĐANG KẾT NỐI " + port.getDescriptivePortName());
            } else {
                // Handle the case where port is null, e.g., show an error message
                stat.setText("Port is not available.");
            }
        } else {
            // Handle the case where the selected index is invalid
            stat.setText("No port selected.");
        }
    }

    public void enter(ActionEvent e) throws IOException {
        // Run if port available
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

            stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
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
            if (ex.getMessage().contains("Port is null")) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setTitle("Cảnh báo");
                    alert.setHeaderText("Không tìm thấy cổng");
                    alert.setContentText("Vui lòng chọn cổng và thử lại.");
                    alert.getButtonTypes().setAll(new ButtonType("Thử Lại", ButtonData.CANCEL_CLOSE));
                    alert.showAndWait();
                });
            } else {
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Lỗi kết nối");
                    alert.setHeaderText("Vui lòng kiểm tra kết nối và thử lại.");
                    alert.setContentText("Không thể kết nối ");
                    alert.getButtonTypes().setAll(new ButtonType("Thử Lại", ButtonData.CANCEL_CLOSE));
                    alert.showAndWait();
                });
            }
        }
    }

    public SerialPort getPort() {
        return port;
    }
}