import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ControllerGeneral {
    private Stage stage;
    private Scene scene;
    private Gauge gauge;

    // ScreenGeneral
    @FXML private AnchorPane generalPane;
    @FXML private HBox batteryBox;
    @FXML private Pane errorPane;
    @FXML private Label maxVLabel, minVLabel, delVLabel, sumVLabel, avgVLabel, maxTLabel, avgTLabel;
    @FXML private Label numFaultLabel, faultLabel;

    // ScreenDetail
    @FXML private GridPane cellPane;

    // ScreenProfile
    @FXML private GridPane profilePane;
    @FXML private Label maxVPro, minVPro, sumMaxVPro, sumMinVPro, difVPro, maxTPro;
    @FXML private TextField maxVProText, minVProText, difVProText, sumMaxVProText, sumMinVProText, maxTProText;
    @FXML private Label typeLabel, numCellLabel, ratioLabel, chargeLabel, drainLabel, capacityLabel;

    final private String[] screen = {"General", "Detail", "Profile"};
    private String currentScreen;

    private Excel excel;

    int numCell;
    private Hashtable<String, String> characteristics;

    // Note: JavaFX optimization doesn't rerender old properties
    public void initialize() {
        currentScreen = screen[0];

        initCharacteristics();

        // Add SoC gauge on ScreenGeneral
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

        // Create excel
        try {
            excel = new Excel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void back(ActionEvent e) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("ScreenMain.fxml"));
        stage = (Stage)((Node) e.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void general(ActionEvent e) {
        currentScreen = screen[0];

        for (Node node : findNodesByClass(generalPane, "general")) {
            node.setVisible(true);
        }

        cellPane.setVisible(false);

        errorPane.setVisible(true);

        profilePane.setVisible(false);
    }

    public void detail(ActionEvent e) {
        currentScreen = screen[1];

        for (Node node : findNodesByClass(generalPane, "general")) {
            node.setVisible(false);  // Hide nodes on ScreenGeneral
        }

        cellPane.setVisible(true);

        errorPane.setVisible(true);

        profilePane.setVisible(false);
    }

    public void profile(ActionEvent e) {
        currentScreen = screen[2];

        for (Node node : findNodesByClass(generalPane, "general")) {
            node.setVisible(false);  // Hide nodes on ScreenGeneral
        }

        cellPane.setVisible(false);

        errorPane.setVisible(false);

        profilePane.setVisible(true);
    }

    public void processData(JSONArray dataArray, String timestamp) {
        numCell = dataArray.length();
        characteristics.put("numCell", String.valueOf(numCell));

        // TODO: get battery characteristics
        // characteristics.put(...);
        updateCharacteristics();
        
        try {
            Hashtable<String, Double> maxmin = calculateMaxMin(dataArray);

            // Append to excel
            excel.write(dataArray, timestamp, maxmin);

            // Only process data for current screen
            if (currentScreen == screen[0]) {
                dataScreenGeneral(dataArray);
            } else if (currentScreen == screen[1]) {
                dataScreenDetail(dataArray);
            } else {
                dataScreenProfile(dataArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Update ScreenGeneral
    private void dataScreenGeneral(JSONArray dataArray) throws JSONException {
        Hashtable<String, Double> maxmin = calculateMaxMin(dataArray);

        final double maxV = maxmin.get("maxV");
        final double minV = maxmin.get("minV");
        final double sumV = maxmin.get("sumV");
        final double delV = maxV - minV;
        final double avgV = sumV / numCell;

        final double maxT = maxmin.get("maxT");
        final double avgT = maxmin.get("sumT") / numCell;

        // Display voltage and temperature data
        Platform.runLater(() -> {
            maxVLabel.setText(String.format("%.2f", maxV) + "V");
            minVLabel.setText(String.format("%.2f", minV) + "V");
            delVLabel.setText(String.format("%.2f", delV) + "V");
            sumVLabel.setText(String.format("%.2f", sumV) + "V");
            avgVLabel.setText(String.format("%.2f", avgV) + "V");
            avgVLabel.setText(String.format("%.2f", avgV) + "V");
            
            maxTLabel.setText(String.format("%.2f", maxT) + "°C");
            avgTLabel.setText(String.format("%.2f", avgT) + "°C");
        });

        // updateFault(maxmin);
    }

    // Update ScreenDetail
    private void dataScreenDetail(JSONArray dataArray) throws JSONException {
        List<Label> cellLabels = findLabels((Parent) cellPane);
        List<Node> imageViewNodes = findNodesByClass(cellPane, "detailImage");
        List<Node> dataBoxes = findNodesByClass(cellPane, "detailDataBox");

        Hashtable<String, Double> maxmin = calculateMaxMin(dataArray);

        for (int i = 1; i <= numCell; i++) {
            String state = "normal";

            JSONObject dataObject = dataArray.getJSONObject(i-1);

            double voltage = dataObject.optDouble("voltage", Double.NaN);
            double temperature = dataObject.optDouble("temperature", Double.NaN);

            int cellNo = i;

            // Update cell number label
            Platform.runLater(() -> cellLabels.get(cellNo-1).setText("Cell " + cellNo));

            if (temperature == maxmin.get("maxT")) {
                state = "hot";
            }

            if (voltage == maxmin.get("maxV")) {
                state = "max";
            } else if (voltage == maxmin.get("minV")) {
                state = "min";
            }

            updateCellImage((ImageView) imageViewNodes.get(cellNo-1), state);

            updateDataLabels((Parent) dataBoxes.get(cellNo-1), voltage, temperature, state);

            // System.out.println("Cell " + i + ": " + "Voltage: " + voltage + ", Temperature: " + temperature);
        }

        // updateFault(maxmin);
    }

    private void dataScreenProfile(JSONArray dataArray) throws JSONException {
        for (int i = 1; i <= numCell; i++) {
            JSONObject dataObject = dataArray.getJSONObject(i-1);
            Double ov = dataObject.optDouble("ov", Double.NaN);
            Double uv = dataObject.optDouble("uv", Double.NaN);

            Double os = ov * numCell;
            Double us = uv * numCell;

            Double ot = dataObject.optDouble("ot", Double.NaN);
            Double dv = dataObject.optDouble("dv", Double.NaN);

            maxVPro.setText(String.valueOf(ov));
            minVPro.setText(String.valueOf(uv));
            sumMaxVPro.setText(String.valueOf(os));
            sumMinVPro.setText(String.valueOf(us));
            maxTPro.setText(String.valueOf(ot));
            difVPro.setText(String.valueOf(dv));
        }

        Platform.runLater(() -> numCellLabel.setText(characteristics.get("numCell")));
    }

    private Hashtable<String, Double> calculateMaxMin(JSONArray dataArray) throws JSONException {
        // Reinitialize data
        double maxV = 0;
        double minV = 0;
        double sumV = 0;
        double maxT = 0;
        double sumT = 0;

        for (int i = 0; i < numCell; i++) {
            JSONObject dataObject = dataArray.getJSONObject(i);

            double voltage = dataObject.optDouble("voltage", Double.NaN);
            double temperature = dataObject.optDouble("temperature", Double.NaN);
            
            if (i == 0) {
                int SOC = dataObject.optInt("SOC", 0);

                // Display SOC
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

        Hashtable<String, Double> maxmin = new Hashtable<>();
        
        maxmin.put("maxV", maxV);
        maxmin.put("minV", minV);
        maxmin.put("maxT", maxT);
        maxmin.put("sumV", sumV);
        maxmin.put("sumT", sumT);

        return maxmin;
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

    // TODO: test
    private void updateCellImage(ImageView node, String state) {
        String url = "images/" + state + ".png";

        Image image = new Image(getClass().getResourceAsStream(url));
        Platform.runLater(() -> node.setImage(image));
    }

    private void updateDataLabels(Parent box, double V, double T, String state) {
        List<Label> labels = findLabels(box);

        Platform.runLater(() -> {
            // Voltage label 2 decimal places
            labels.get(0).setText(String.format("%.2f", V) + "V");

            // Temperature label 1 decimal place
            labels.get(1).setText(String.format("%.1f", T) + "°C");

            // If cell is blue, make labels white
            if (state == "min") {
                labels.get(0).setTextFill(Color.WHITE);
                labels.get(1).setTextFill(Color.WHITE);
            } else {
                labels.get(0).setTextFill(Color.BLACK);
                labels.get(1).setTextFill(Color.BLACK);
            }
        });
    }

    public void save(ActionEvent e) throws FileNotFoundException, IOException {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showSaveDialog(null);

        if (selectedFile != null) {
            FileChooser.ExtensionFilter excelFilter = new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx");
            fileChooser.getExtensionFilters().add(excelFilter);
            
            // Restrict the dialog to only show the Excel filter
            fileChooser.setSelectedExtensionFilter(excelFilter);

            // Proceed with saving the Excel file to the selected path
            XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(excel.fileName));
  
            // Write the workbook to the chosen file
            FileOutputStream outputStream = new FileOutputStream(selectedFile);
            workbook.write(outputStream);
            outputStream.close();
            workbook.close();
            
            System.out.println("Excel file saved successfully!");
        } else {
            // Handle case where user cancels the dialog
            System.out.println("File saving cancelled.");
        }
    }

    private void initCharacteristics() {
        characteristics = new Hashtable<>();
        characteristics.put("type", "Lifepo4");
        characteristics.put("ratio", "20C");
        characteristics.put("charge", "15A");
        characteristics.put("drain", "560A");
        characteristics.put("capacity", "100Ah");
    }

    // Display battery characteristics
    private void updateCharacteristics() {
        Platform.runLater(() -> {
            typeLabel.setText(characteristics.get("type"));
            ratioLabel.setText(characteristics.get("ratio"));
            chargeLabel.setText(characteristics.get("charge"));
            drainLabel.setText(characteristics.get("drain"));
            capacityLabel.setText(characteristics.get("capacity"));
        });
    }

    public void popEdit(ActionEvent e) throws IOException {
        Label label = (Label) e.getSource();
        label.setVisible(false);
        switch (label.getId()) {
            case "maxVPro":
                maxVProText.setVisible(true);
                maxVProText.setText(maxVPro.getText());
                break;
            case "minVPro":
                minVProText.setVisible(true);
                minVProText.setText(minVPro.getText());
                break;
            case "difVPro":
                difVProText.setVisible(true);
                difVProText.setText(difVPro.getText());
                break;
            case "sumMaxVPro":
                sumMaxVProText.setVisible(true);
                sumMaxVProText.setText(sumMaxVPro.getText());
                break;
            case "sumMinVPro":
                sumMinVProText.setVisible(true);
                sumMinVProText.setText(sumMinVPro.getText());
                break;
            case "maxTPro":
                maxTProText.setVisible(true);
                maxTProText.setText(maxTPro.getText());
                break;
            default:
                break;
        }
    }

    public void finishEdit(KeyEvent e) throws IOException {
        TextField text = (TextField) e.getSource();
            if (e.getCode() == KeyCode.ENTER) {
            text.setVisible(false);
            switch (text.getId()) {
                case "maxVProText":
                    maxVPro.setVisible(true);
                    maxVPro.setText(text.getText());
                    break;
                case "minVProText":
                    minVPro.setVisible(true);
                    minVPro.setText(text.getText());
                    break;
                case "difVProText":
                    difVPro.setVisible(true);
                    difVPro.setText(text.getText());
                    break;
                case "sumMaxVProText":
                    sumMaxVPro.setVisible(true);
                    sumMaxVPro.setText(text.getText());
                    break;
                case "sumMinVProText":
                    sumMinVPro.setVisible(true);
                    sumMinVPro.setText(text.getText());
                    break;
                case "maxTProText":
                    maxTPro.setVisible(true);
                    maxTPro.setText(text.getText());
                    break;
                default:
                    break;
            }
        }
    }

    private void updateFault(Hashtable<String, Double> maxmin) {
        ArrayList<String> fault = new ArrayList<>();

        double cellHigh = Double.parseDouble(maxVPro.textProperty().getValue());
        double cellLow = Double.parseDouble(minVPro.textProperty().getValue());
        double sumHigh = Double.parseDouble(sumMaxVPro.textProperty().getValue());
        double sumLow = Double.parseDouble(sumMinVPro.textProperty().getValue());
        double diffVolt = Double.parseDouble(difVPro.textProperty().getValue());
        double tempP = Double.parseDouble(maxTPro.textProperty().getValue());

        if (maxmin.get("maxV") >= cellHigh) {
            fault.add("Bảo vệ điện áp cao");
        }

        if (maxmin.get("minV") <= cellLow) {
            fault.add("Bảo vệ điện áp thấp");
        }

        if (maxmin.get("sumV") >= sumHigh) {
            fault.add("Bảo vệ tổng điện áp cao");
        }
    
        if (maxmin.get("sumV") <= sumLow) {
            fault.add("Bảo vệ tổng điện áp thấp");
        }
    
        if (maxmin.get("delV") > diffVolt) {
            fault.add("Bảo vệ chênh lệch áp");
        }
    
        if (maxmin.get("maxT") >= tempP) {
            fault.add("Bảo vệ nhiệt độ cao");
        }

        numFaultLabel.setText(String.valueOf(fault.size()));
        faultLabel.setText(fault.stream().collect(Collectors.joining("\n")));
    }

    public void manual(ActionEvent e) throws IOException {
        Parent manual = FXMLLoader.load(getClass().getResource("Manual.fxml"));

        Stage mStage = new Stage();
        mStage.setTitle("Hướng dẫn sử dụng");

        mStage.initOwner(((Node) e.getSource()).getScene().getWindow());

        Scene infoScene = new Scene(manual);
        mStage.setScene(infoScene);

        mStage.show();
    }
}
