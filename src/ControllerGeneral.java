import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Gauge.SkinType;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.Section;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ControllerGeneral {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private Gauge gauge;
    @FXML private HBox batteryBox;
    @FXML private GridPane cellPane;

    public void initialize() {
        gauge = GaugeBuilder.create()
        .skinType(SkinType.BATTERY)
        .animated(true)
        .sectionsVisible(true)
        .sections(new Section(0, 10, Color.RED),
                    new Section(10, 20, Color.rgb(255,235,59)), //YELLOW
                    new Section(20, 100, Color.GREEN))
        .build();

        DataReader reader = new DataReader(this, Controller.getUSB());
        Thread thread = new Thread(reader);
        thread.start();
    }

    public void back(ActionEvent e) throws IOException {
        root = FXMLLoader.load(getClass().getResource("ScreenMain.fxml"));
        stage = (Stage)((Node) e.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void thongtinchitiet(ActionEvent e) {

    }

    public void processSensorData(JSONArray dataArray) throws JSONException {
        for (int i = 0; i < dataArray.length(); i++) {
            // Label cellLabel = (Label) cellPane.lookup("#cell" + i + "Label");
            // cellLabel.setText("Cell " + i);

            JSONObject dataObject = dataArray.getJSONObject(i);
            double voltage = dataObject.optDouble("voltage", Double.NaN);
            double temperature = dataObject.optDouble("temperature", Double.NaN);
            
            if (i == 0) {
                int SOC = dataObject.optInt("SOC", 0);
                Platform.runLater(() -> {
                    gauge.setValue(SOC);
                    batteryBox.getChildren().add(gauge);
                });

                System.out.println("! Battery Level: " + SOC + "%");
            }
            System.out.println("! Cell " + i + ": " + "Voltage: " + voltage + ", Temperature: " + temperature);
        }
    }
}
