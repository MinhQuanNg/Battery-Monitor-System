import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fazecast.jSerialComm.SerialPort;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
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
    private Scene manualScene = null;
    private Stage manualStage = null;

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
    @FXML private Label setting1, setting2, setting5, setting6;
    @FXML private Label maxVPro, minVPro, sumMaxVPro, sumMinVPro, difVPro, maxTPro;
    @FXML private Button set1, set2, set5, set6, save;
    @FXML private TextField maxVProText, minVProText, difVProText, maxTProText;
    @FXML private Label typeLabel, numCellLabel, ratioLabel, chargeLabel, drainLabel, capacityLabel;

    final private String[] screen = {"General", "Detail", "Profile"};
    private String currentScreen;

    private Excel excel;
    List<TextField> textFields = Arrays.asList(minVProText, difVProText, maxTProText);
    private int numCell;
    private Hashtable<String, String> characteristics;
    private double ov, uv, os, us, ot, dv;
    private SerialPort USB;

    public void startThread(Controller controller) {
        USB = controller.getUSB();
        DataReader reader = new DataReader(this, USB);

        // Start thread to read data
        Thread thread = new Thread(reader);
        controller.setThread(thread);

        thread.start();
    }

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

    public void manual(ActionEvent e) throws IOException {
        // Load the manual scene only once and reuse if already loaded
        if (manualScene == null) {
            Parent manual = FXMLLoader.load(getClass().getResource("Manual.fxml"));
            manualScene = new Scene(manual);
        }

        // Use a single instance of the manual stage if it's already been created
        if (manualStage == null) {
            manualStage = new Stage();
            manualStage.setTitle("Hướng dẫn sử dụng");
            manualStage.initOwner(((Node) e.getSource()).getScene().getWindow());
            manualStage.setScene(manualScene);
        }

        manualStage.show();
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

        Platform.runLater(() -> numCellLabel.setText(characteristics.get("numCell")));

        // TODO: get battery characteristics
        // characteristics.put(...);
        updateCharacteristics();
        
        try {
            Hashtable<String, Double> maxmin = calculateMaxMin(dataArray);

            // Get protect params
            JSONObject dataObject = dataArray.getJSONObject(0);
            ov = dataObject.optDouble("ov", Double.NaN);
            uv = dataObject.optDouble("uv", Double.NaN);
            os = ov * numCell;
            us = uv * numCell;
            ot = dataObject.optDouble("ot", Double.NaN);
            dv = dataObject.optDouble("dv", Double.NaN);

            // Append to excel
            excel.write(dataArray, timestamp, maxmin);

            // Only process data for current screen
            if (currentScreen == screen[0]) {
                dataScreenGeneral(dataArray, maxmin);
            } else if (currentScreen == screen[1]) {
                dataScreenDetail(dataArray, maxmin);
            } else {
                dataScreenProfile(dataArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Update ScreenGeneral
    private void dataScreenGeneral(JSONArray dataArray, Hashtable<String, Double> maxmin) throws JSONException {
        final double maxV = maxmin.get("maxV");
        final double minV = maxmin.get("minV");
        final double sumV = maxmin.get("sumV");
        final double delV = maxmin.get("delV");
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

        updateFault(maxmin);
    }

    // Update ScreenDetail
    private void dataScreenDetail(JSONArray dataArray, Hashtable<String, Double> maxmin) throws JSONException {
        List<Label> cellLabels = findLabels((Parent) cellPane);
        List<Node> imageViewNodes = findNodesByClass(cellPane, "detailImage");
        List<Node> dataBoxes = findNodesByClass(cellPane, "detailDataBox");

        for (int i = 0; i < numCell; i++) {
            String state = "normal";

            JSONObject dataObject = dataArray.getJSONObject(i);

            double voltage = dataObject.optDouble("voltage", Double.NaN);
            double temperature = dataObject.optDouble("temperature", Double.NaN);

            int cellNo = i + 1;

            // Update cell number label
            Platform.runLater(() -> cellLabels.get(cellNo).setText("Cell " + cellNo));

            if (temperature == maxmin.get("maxT")) {
                state = "hot";
            }

            if (voltage == maxmin.get("maxV")) {
                state = "max";
            } else if (voltage == maxmin.get("minV")) {
                state = "min";
            }

            updateCellImage((ImageView) imageViewNodes.get(cellNo), state);

            updateDataLabels((Parent) dataBoxes.get(cellNo), voltage, temperature, state);

            // System.out.println("Cell " + i + ": " + "Voltage: " + voltage + ", Temperature: " + temperature);
        }

        updateFault(maxmin);
    }

    private void dataScreenProfile(JSONArray dataArray) throws JSONException {
        Platform.runLater(() -> {
            maxVPro.setText(String.valueOf(ov));
            minVPro.setText(String.valueOf(uv));
            sumMaxVPro.setText(String.valueOf(os));
            sumMinVPro.setText(String.valueOf(us));
            maxTPro.setText(String.valueOf(ot));
            difVPro.setText(String.valueOf(dv));
        });
    }

    private Hashtable<String, Double> calculateMaxMin(JSONArray dataArray) throws JSONException {
        // Reinitialize data
        double maxV = 0;
        double minV = 0;
        double sumV = 0;
        double maxT = 0;
        double sumT = 0;
        double delV;

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

        delV = maxV - minV;

        Hashtable<String, Double> maxmin = new Hashtable<>();
        
        maxmin.put("maxV", maxV);
        maxmin.put("minV", minV);
        maxmin.put("maxT", maxT);
        maxmin.put("delV", delV);
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
            String fileName = selectedFile.getName();
            if (!fileName.endsWith(".xlsx")) {
              fileName += ".xlsx";
            }
            selectedFile = new File(selectedFile.getParent(), fileName);
            
            // Proceed with saving the Excel file to the selected path
            XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(excel.getFileName()));
  
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
        Platform.runLater(() -> {
            System.out.println("hi");
        Button btn = (Button) e.getSource();
        TextField targetTextField = null;
        Label sourceLabel = null;

        switch (btn.getId()) {
            case "set1":
                targetTextField = maxVProText;
                sourceLabel = maxVPro;
                break;
            case "set2":
                targetTextField = minVProText;
                sourceLabel = minVPro;
                break;
            case "set5":
                targetTextField = difVProText;
                sourceLabel = difVPro;
                break;
            case "set6":
                targetTextField = maxTProText;
                sourceLabel = maxTPro;
                break;
        }

        if (targetTextField != null && sourceLabel != null) {
            updateVisibilityAndFocus(btn, targetTextField, sourceLabel);
            }
        });
    }
    

    private void updateVisibilityAndFocus(Button btn, TextField textField, Label label) {
        save.setVisible(true);
        textField.setVisible(true);
        textField.setText(label.getText());
        textField.requestFocus();
    }

    public void finishEdit(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            TextField text = (TextField) e.getSource();
            showSaveConfirmationPopup(text);
        }
    }

    
    public void onSaveAllChangesAction() {
        // StringBuilder allData = new StringBuilder();
        // // Assuming you have TextField instances for each of your inputs
        // TextField[] textFields = {maxVProText, minVProText, difVProText, maxTProText,
        // sumMaxVProText, sumMinVProText};
        // for (TextField textField : textFields) {
        // String formattedData = formatData(textField.getText(), textField); //
        // Assuming formatData method takes the text value and the TextField itself
        // allData.append(formattedData).append("\n"); // Append a newline or other
        // delimiter as needed
        // }
        // writeBoard(allData.toString());
    }

    private String getCorrespondingLabel(String textFieldId) {
        // Example mapping, replace with actual logic
        switch (textFieldId) {
            case "maxVProText":
                return setting1.getText();
            case "minVProText":
                return setting2.getText();
            case "difVProText":
                return setting5.getText();
            case "maxTProText":
                return setting6.getText();
            default:
                return null;
        }
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?"); // Match a number with optional '-' and decimal.
    }

    private void showSaveConfirmationPopup(TextField textField) {
        // IDs that require numeric input
        List<String> numericFields = Arrays.asList("maxVProText");
        if (numericFields.contains(textField.getId()) && !isNumeric(textField.getText())) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Invalid Input");
            errorAlert.setHeaderText("Invalid Number Format");
            errorAlert.setContentText(
                    "Please enter a valid number for \"" + getCorrespondingLabel(textField.getId()) + "\"" + ".");
            errorAlert.showAndWait();
            return; // Exit the method early
        }
    
        // Create a custom ButtonType for "Save"
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, "", saveButtonType, ButtonType.CANCEL);
        confirmationAlert.setTitle("Confirm Changes");
        confirmationAlert.setHeaderText("Bạn có chắc chắn muốn lưu thay đổi");
        confirmationAlert.setContentText(getCorrespondingLabel(textField.getId()) + "  " + "\"" + textField.getText() + "\"");
    
        // Show the alert and wait for response
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == saveButtonType) {
                writeBoard(formatAndWriteValue(textField, textField.getText()));
                updateLabel(textField);
            } else {
            textField.setVisible(false); // Hide the text field if not saved
            save.setVisible(false);
        }
    });
}

    private void updateLabel(TextField textField) {
        Label targetLabel = null;
        switch (textField.getId()) {
            case "maxVProText":
                targetLabel = maxVPro;
                // Assuming numCell is already defined and converted to a numeric value
                try {
                    double maxVProValue = Double.parseDouble(textField.getText());
                    sumMaxVPro.setText(String.format("%.2f", maxVProValue * numCell)); // Corrected this line
                } catch (NumberFormatException e) {
                    // Handle invalid input
                    sumMaxVPro.setText("Invalid input");
                }
                break;
            case "minVProText":
                targetLabel = minVPro;
                try {
                    double minVProValue = Double.parseDouble(textField.getText());
                    sumMinVPro.setText(String.format("%.2f", minVProValue * numCell)); // Added this line
                } catch (NumberFormatException e) {
                    // Handle invalid input
                    sumMinVPro.setText("Invalid input");
                }
                break;
            case "difVProText":
                targetLabel = difVPro;
                break;
            case "maxTProText":
                targetLabel = maxTPro;
                break;
        }
    
        if (targetLabel != null) {
            targetLabel.setText(textField.getText());
            targetLabel.setVisible(true);
            textField.setVisible(false); 
        }
    }

    private void updateFault(Hashtable<String, Double> maxmin) {
        ArrayList<String> fault = new ArrayList<>();

        if (maxmin.get("maxV") >= ov) {
            fault.add("Bảo vệ điện áp cao");
        }

        if (maxmin.get("minV") <= uv) {
            fault.add("Bảo vệ điện áp thấp");
        }

        if (maxmin.get("sumV") >= os) {
            fault.add("Bảo vệ tổng điện áp cao");
        }
    
        if (maxmin.get("sumV") <= us) {
            fault.add("Bảo vệ tổng điện áp thấp");
        }
    
        if (maxmin.get("delV") > dv) {
            fault.add("Bảo vệ chênh lệch áp");
        }
    
        if (maxmin.get("maxT") >= ot) {
            fault.add("Bảo vệ nhiệt độ cao");
        }

        Platform.runLater(() -> {
            numFaultLabel.setText(String.valueOf(fault.size()));
            faultLabel.setText(fault.stream().collect(Collectors.joining("\n")));
        });
    }


    private String formatAndWriteValue(TextField textField, String inputValue) {
        String output = "";
        double setValue = Double.parseDouble(inputValue);

        switch (textField.getId()) {
            case "maxVProText":
                output = String.format("%d.o", (int) (setValue * 100));
                break;
            case "minVProText":
                output = String.format("%d.u", (int) (setValue * 100));
                break;
            case "diffVProText":
                output = String.format("%d.d", (int) (setValue * 100));
                break;
            case "maxTProText":
                output = String.format("%d.t", (int) (setValue * 10));
                break;
        }

        return output;
    }

    public void writeBoard(String data) {
        try {
            if (USB == null) {
                System.out.println("No USB found.");
                return;
            }else{            
                byte[] bytes = data.getBytes();
                USB.writeBytes(bytes, bytes.length);}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}