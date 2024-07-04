import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ControllerGeneral {
    private Stage stage;
    private Scene scene;
    private Gauge gauge;

    @FXML private AnchorPane generalPane;
    @FXML private HBox batteryBox;
    @FXML private GridPane cellPane;
    @FXML private Label maxVLabel, minVLabel, delVLabel, sumVLabel, avgVLabel, maxTLabel, avgTLabel;

    private double maxV, minV, sumV, maxT, sumT;

    public void initialize() throws IOException {
        gauge = GaugeBuilder.create()
        .skinType(SkinType.BATTERY)
        .animated(true)
        .sectionsVisible(true)
        .sections(new Section(0, 10, Color.RED),
                    new Section(10, 20, Color.rgb(255,235,59)), //YELLOW
                    new Section(20, 100, Color.GREEN))
        .build();

        batteryBox.getChildren().add(gauge);

        // TODO: get original USB
        DataReader reader = new DataReader(this, Controller.getUSB());

        // Start reading data in separate thread
        Thread thread = new Thread(reader);
        thread.start();
    }

    public void back(ActionEvent e) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("ScreenMain.fxml"));
        stage = (Stage)((Node) e.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void thongtinchitiet(ActionEvent e) {
        for (Node node : findNodesByClass(generalPane, "general")) {
            node.setVisible(false);  // Hide nodes on screenGeneral
        }

        cellPane.setVisible(true);
    }

    public void processData(JSONArray dataArray) throws JSONException {
        dataScreenGeneral(dataArray);
        dataScreenDetail(dataArray);
    }

    // TODO: Refactor
    // Update screenGeneral
    private void dataScreenGeneral(JSONArray dataArray) throws JSONException {
        final double finaldelV, finalavgV, finalavgT;
        int noCells = dataArray.length();
        
        // Reinitialize data
        maxV = 0;
        minV = 0;
        sumV = 0;
        maxT = 0;
        sumT = 0;

        for (int i = 0; i < noCells; i++) {
            JSONObject dataObject = dataArray.getJSONObject(i);

            double voltage = dataObject.optDouble("voltage", Double.NaN);
            double temperature = dataObject.optDouble("temperature", Double.NaN);
            
            if (i == 0) {
                // Dung luong
                int SOC = dataObject.optInt("SOC", 0);
                // Display dung luong
                Platform.runLater(() -> gauge.setValue(SOC));

                // System.out.println("Battery Level: " + SOC + "%");

                maxV = voltage;
                minV = voltage;
                maxT = temperature;
            }

            sumV += voltage;
            sumT += temperature;

            if (voltage > maxV) {
                maxV = voltage;
            } else if (voltage < minV) {
                minV = voltage;
            }

            if (temperature > maxT) {
                maxT = temperature;
            }
        }

        final double finalmaxV = maxV;
        final double finalminV = minV;
        final double finalsumV = sumV;
        finaldelV = maxV - minV;
        finalavgV = sumV / noCells;

        final double finalmaxT = maxT;
        finalavgT = sumT / noCells;

        // Display voltage and temperature data in ScreenGeneral
        Platform.runLater(() -> {
            maxVLabel.setText(String.format("%.2f", finalmaxV) + "V");
            minVLabel.setText(String.format("%.2f", finalminV) + "V");
            delVLabel.setText(String.format("%.2f", finaldelV) + "V");
            sumVLabel.setText(String.format("%.2f", finalsumV) + "V");
            avgVLabel.setText(String.format("%.2f", finalavgV) + "V");
            avgVLabel.setText(String.format("%.2f", finalavgV) + "V");
            
            maxTLabel.setText(String.format("%.2f", finalmaxT) + "°C");
            avgTLabel.setText(String.format("%.2f", finalavgT) + "°C");
        });
    }

    // Update screenDetail
    private void dataScreenDetail(JSONArray dataArray) throws JSONException {
        int noCells = dataArray.length();

        List<Label> cellLabels = findLabels((Parent) cellPane);
        List<Node> imageViewNodes = findNodesByClass(cellPane, "detailImage");
        List<Node> dataBoxes = findNodesByClass(cellPane, "detailDataBox");

        for (int i = 1; i <= noCells; i++) {
            JSONObject dataObject = dataArray.getJSONObject(i-1);

            double voltage = dataObject.optDouble("voltage", Double.NaN);
            double temperature = dataObject.optDouble("temperature", Double.NaN);

            int cellNo = i;

            // Update cell number label
            Platform.runLater(() -> cellLabels.get(cellNo-1).setText("Cell " + cellNo));

            updateCellImage((ImageView) imageViewNodes.get(cellNo-1), voltage, temperature);
            updateDataLabels((Parent) dataBoxes.get(cellNo-1), voltage, temperature);

            // System.out.println("Cell " + i + ": " + "Voltage: " + voltage + ", Temperature: " + temperature);
        }
    }

    private List<Node> findNodesByClass(Parent root, String className) {
        List<Node> matchingNodes = new ArrayList<>();
        for (Node node : root.getChildrenUnmodifiable()) {
          if (node.getStyleClass().contains(className)) {
            matchingNodes.add(node);
          }
          if (node instanceof Parent) {
            matchingNodes.addAll(findNodesByClass((Parent) node, className));
          }
        }
        return matchingNodes;
    }

    // not recursive
    private List<Label> findLabels(Parent root) {
        List<Label> labels = new ArrayList<>();
        for (Node node : root.getChildrenUnmodifiable()) {
          if (node instanceof Label) {
            labels.add((Label) node);
          }
        }
        return labels;
    }

    private void updateCellImage(ImageView node, double V, double T) {
        String url = "images/normal.png";

        if (T == maxT) {
            url = "images/hot.png";
        }

        if (V == maxV) {
            url = "images/max.png";
        } else if (V == minV) {
            url = "images/min.png";
        }

        Image image = new Image(getClass().getResourceAsStream(url));
        Platform.runLater(() -> node.setImage(image));
    }

    private void updateDataLabels(Parent box, double V, double T) {
        List<Label> labels = findLabels(box);

        Platform.runLater(() -> {
            // Voltage label 2 decimal places
            labels.get(0).setText(String.format("%.2f", V) + "V");

            // Temperature label 1 decimal place
            labels.get(1).setText(String.format("%.1f", T) + "°C");
        });
    }
}
