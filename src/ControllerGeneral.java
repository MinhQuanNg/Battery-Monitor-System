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
    @FXML private Label maxVLabel, minVLabel, delVLabel, sumVLabel, avgVLabel, maxTLabel, avgTLabel;
    @FXML private GridPane cellPane;

    private double maxV = 0, minV = 0, sumV = 0, maxT = 0, sumT = 0;


    public void initialize() {
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
        for (Node node : findNodesByClass(root, "general")) {
            node.setVisible(false);  // Change visibility to hidden
        }

        cellPane.setVisible(true);
    }

    public void processData(JSONArray dataArray) throws JSONException {
        dataScreenGeneral(dataArray);
        dataScreenDetail(dataArray);
    }


    // TODO: Refactor
    private void dataScreenGeneral(JSONArray dataArray) throws JSONException {
        final double finaldelV, finalavgV, finalavgT;
        int noCells = dataArray.length();

        for (int i = 0; i < noCells; i++) {
            JSONObject dataObject = dataArray.getJSONObject(i);

            double voltage = dataObject.optDouble("voltage", Double.NaN);
            double temperature = dataObject.optDouble("temperature", Double.NaN);
            
            if (i == 0) {
                // Dung luong
                int SOC = dataObject.optInt("SOC", 0);
                // Display dung luong
                Platform.runLater(() -> gauge.setValue(SOC));

                System.out.println("! Battery Level: " + SOC + "%");

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

    // Update cell image and data
    private void dataScreenDetail(JSONArray dataArray) throws JSONException {
        String url;
        int noCells = dataArray.length();

        for (int i = 1; i <= noCells; i++) {
            JSONObject dataObject = dataArray.getJSONObject(i-1);

            double voltage = dataObject.optDouble("voltage", Double.NaN);
            double temperature = dataObject.optDouble("temperature", Double.NaN);
            
            Label cellLabel = (Label) cellPane.lookup("#cell" + i + "Label");
            cellLabel.setText("Cell " + i);

            // Update cell image
            for (Node image : findNodesByClass(cellPane, "generalImage")) {
                if (temperature == maxT) {
                    url = "file:images/hot.png";
                } else {
                    url = "file:images/normal.png";
                }

                if (voltage == maxV) {
                    url = "file:images/max.png";
                } else if (voltage == minV) {
                    url = "file:images/min.png";
                }
                
                ((ImageView) image).setImage(new Image(url));
            }

            // Update cell voltage and temperature
            for (Node box : findNodesByClass(cellLabel, "generalDataBox")) {
                List<Label> labels = findLabels((Parent) box);
                
                // Voltage label 2 decimal places
                labels.get(0).setText(String.format("%.2f", voltage));

                // Temperature label 1 decimal place
                labels.get(1).setText(String.format("%.1f", temperature));
            }

            System.out.println("! Cell " + i + ": " + "Voltage: " + voltage + ", Temperature: " + temperature);
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

    private List<Label> findLabels(Parent root) {
        List<Label> labels = new ArrayList<>();
        for (Node node : root.getChildrenUnmodifiable()) {
          if (node instanceof Label) {
            labels.add((Label) node);
          }
          if (node instanceof Parent) {
            labels.addAll(findLabels((Parent) node));
          }
        }
        return labels;
      }
}