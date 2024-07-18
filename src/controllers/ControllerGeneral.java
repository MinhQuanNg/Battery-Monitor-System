package controllers;

import constants.*;
import utilities.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
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
import javafx.geometry.HPos;
import javafx.geometry.VPos;
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
import javafx.scene.input.MouseEvent;
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
    private Stage manualStage = null, chartStage = null;

    // ScreenGeneral
    @FXML
    private AnchorPane generalPane;
    @FXML
    private HBox batteryBox;
    @FXML
    private Label maxVLabel, minVLabel, delVLabel, sumVLabel, avgVLabel, maxTLabel, avgTLabel;
    @FXML
    private Label numFaultLabel, faultLabel;

    // ScreenDetail
    @FXML
    private GridPane cellPane;

    // Shared
    @FXML
    private Pane errorPane;

    // ScreenProfile
    @FXML
    private GridPane profilePane;
    @FXML
    private Label setting1, setting2, setting5, setting6;
    @FXML
    private Label maxVPro, minVPro, sumMaxVPro, sumMinVPro, difVPro, maxTPro;
    @FXML
    private Button set1, set2, set5, set6, save, focus;
    @FXML
    private TextField maxVProText, minVProText, difVProText, maxTProText;
    @FXML
    private Label typeLabel, numCellLabel, ratioLabel, chargeLabel, drainLabel, capacityLabel;

    final private String[] screen = { "General", "Detail", "Profile" };
    private String currentScreen;

    private Excel excel;
    private int numCell = 0;
    private Hashtable<String, String> characteristics;
    private double ov, uv, os, us, ot, dv;

    private boolean runCharts = false;
    private SerialPort port;
    private Charts ctrlCharts;
    private boolean firstEnable = true;
    private boolean firstCharts = true;
    private boolean firstCellChart = true;
    private List<Boolean> runCellChart = new ArrayList<>();

    // Note: JavaFX optimization doesn't rerender old properties
    public void initialize() {
        // port.openPort();
        currentScreen = screen[0];
       
        initCharacteristics();
        // Add SoC gauge on ScreenGeneral
        gauge = GaugeBuilder.create()
                .skinType(SkinType.BATTERY)
                .animated(true)
                .prefHeight(generalPane.getHeight() / 2)
                .prefWidth(generalPane.getWidth() / 2)
                .sectionsVisible(true)
                .sections(new Section(0, 10, Color.RED),
                        new Section(10, 20, Color.rgb(255, 235, 59)), // YELLOW
                        new Section(20, 100, Color.GREEN))
                .build();
        
                batteryBox.getChildren().add(gauge);

        // Create excel
        try {
            excel = new Excel();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int rowIndex = 0; rowIndex < cellPane.getRowCount(); rowIndex++) {
            for (int columnIndex = 0; columnIndex < cellPane.getColumnCount(); columnIndex++) {
                Button button = new Button();
                GridPane.setHalignment(button, HPos.CENTER);
                GridPane.setValignment(button, VPos.CENTER);
                button.getStyleClass().add("cell-chart-button");
                
                ImageView imageView = findImageView(rowIndex, columnIndex);
                button.setPrefSize(imageView.getFitWidth(), imageView.getFitHeight());
                
                final int cell = rowIndex * cellPane.getColumnCount() + columnIndex;
                button.onActionProperty().set(e -> {
                    try {
                        CellChart ctrl = initCellChart(cell + 1, getCurrentTimeStamp(new Date()));
                        button.setUserData(ctrl);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    runCellChart.set(cell, true);
                });

                button.setDisable(true);
                cellPane.add(button, columnIndex, rowIndex);
            }
        }
    }

    // TODO: reuse?
    public void back(ActionEvent e) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("../resources/ScreenMain.fxml"));
        stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        scene = new Scene(root, Style.WIDTH, Style.HEIGHT);
        stage.setScene(scene);
        stage.show();
        port.closePort();
    }

    public void manual(ActionEvent e) throws IOException {
        // Load the manual scene only once and reuse if already loaded
        if (manualScene == null) {
            Parent manual = FXMLLoader.load(getClass().getResource("../resources/Manual.fxml"));
            manualScene = new Scene(manual, 600, 400);
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
        errorPane.setVisible(true);

        cellPane.setVisible(false);

        profilePane.setVisible(false);
    }

    public void detail(ActionEvent e) {
        currentScreen = screen[1];
        for (Node node : findNodesByClass(generalPane, "general")) {
            node.setVisible(false); // Hide nodes on ScreenGeneral
        }
        errorPane.setVisible(true);

        cellPane.setVisible(true);

        profilePane.setVisible(false);
    }

    public void profile(ActionEvent e) {
        currentScreen = screen[2];
        List<TextField> TextFields = getAllTextFields(profilePane);
        profilePane.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            Node target = (Node) event.getTarget(); // Get the target of the click event
            if (!(target instanceof Button)) {
                if (!(target instanceof TextField)) {
                    return;
                }
                for (TextField textField : TextFields) {
                    textField.setVisible(false);
                    save.setVisible(false);
                }
            }

        });
        for (Node node : findNodesByClass(generalPane, "general")) {
            node.setVisible(false); // Hide nodes on ScreenGeneral
        }
        errorPane.setVisible(false);

        cellPane.setVisible(false);

        profilePane.setVisible(true);
    
    }

    private ImageView findImageView(int rowIndex, int columnIndex) {
        for (Node node : cellPane.getChildren()) {
            Integer row = GridPane.getRowIndex(node);
            Integer column = GridPane.getColumnIndex(node);
            if (row != null && column != null && row == rowIndex && column == columnIndex) {
                if (node instanceof ImageView) {
                    return (ImageView) node;
                }
            }
        }
        return null;
    }

    public void processData(JSONArray dataArray, Date now) {
        String timestamp = getCurrentTimeStamp(now);

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

            // Enable chart button when data is available
            if (firstEnable) {
                generalPane.lookup("#chartButton").setDisable(false);
                firstEnable = false;
            }

            // Init axes and series for charts
            if (runCharts) {
                if (firstCharts) {
                    Platform.runLater(() -> {
                        ctrlCharts.initSeries(maxmin);
                        chartStage.show();
                    });

                    firstCharts = false;
                }

                // Update charts
                ctrlCharts.updateSeries(maxmin, now);
            }

            // Enable cell chart buttons
            if (firstCellChart) {
                for (int cell = 0; cell < numCell; cell++) {
                    runCellChart.add(false);
                    Button button = getButton(cell);

                    if (button != null) {
                        button.setDisable(false);
                    } else {
                        System.out.println("Button not found.");
                    }
                }

                firstCellChart = false;
            }

            // Update opened cell charts
            for (int cell = 0; cell < numCell; cell++) {
                if (runCellChart.get(cell)) {
                    CellChart ctrl = (CellChart) getButton(cell).getUserData();
                    ctrl.updateSeries(dataArray.getJSONObject(cell).optDouble("voltage", Double.NaN),
                            dataArray.getJSONObject(cell).optDouble("temperature", Double.NaN), now);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Button getButton(int cell) {
        int row = cell / cellPane.getColumnCount();
        int column = cell % cellPane.getColumnCount();
        Button button = (Button) cellPane.getChildren().stream()
                .filter(node -> {
                    Integer rowIdx = GridPane.getRowIndex(node);
                    Integer colIdx = GridPane.getColumnIndex(node);
                    return (node instanceof Button) &&
                            rowIdx != null && rowIdx == row &&
                            colIdx != null && colIdx == column;
                })
                .findFirst()
                .orElse(null);

        return button;
    }

    private static String getCurrentTimeStamp(Date now) {
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy (HH:mm:ss)");
        String strDate = sdfDate.format(now);
        return strDate;
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

            int cellNo = i;

            // Update cell number label
            Platform.runLater(() -> cellLabels.get(cellNo).setText("Cell " + (cellNo + 1)));

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

            // System.out.println("Cell " + i + ": " + "Voltage: " + voltage + ",
            // Temperature: " + temperature);
        }

        updateFault(maxmin);
    }

    private void dataScreenProfile(JSONArray dataArray) throws JSONException {
        Platform.runLater(() -> {
            maxVPro.setText(String.valueOf(ov));
            minVPro.setText(String.valueOf(uv));
            sumMaxVPro.setText(String.format("%.2f", os));
            sumMinVPro.setText(String.format("%.2f", us));
            maxTPro.setText(String.valueOf(ot));
            difVPro.setText(String.valueOf(dv));
        });
    }

    private Hashtable<String, Double> calculateMaxMin(JSONArray dataArray) throws JSONException {
        // Initialize data
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

    public List<TextField> getAllTextFields(Parent root) {
        List<TextField> textFields = new ArrayList<>();
        findAllTextFields(root, textFields);
        return textFields;
    }

    private void findAllTextFields(Parent parent, List<TextField> textFields) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof TextField) {
                textFields.add((TextField) node);
            } else if (node instanceof Parent) {
                findAllTextFields((Parent) node, textFields);
            }
        }
    }

    private void updateCellImage(ImageView node, String state) {
        String url = "../images/" + state + ".png";

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
            if (state.equals("min")) {
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
        // Set the initial filename (optional)
        fileChooser.setInitialFileName("BMS Data " + getCurrentTimeStamp(new Date()) + ".xlsx");
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
        // focus.setDisable(false);
    }

    // @Override
    // public void mouseExited(MouseEvent e) {
    // profilePane.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
    // List<TextField> textFieldsToSave = getAllTextFields(profilePane);
    // for (TextField textField : textFieldsToSave) {
    // if (textField.isVisible()) {
    // if (!(event.getTarget() instanceof TextField) || event.getTarget() !=
    // textField) {
    // textField.setVisible(false);
    // }
    // }
    // }
    // });
    // });
    // }

    public void finishEdit(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            SaveChanges(null);
        }
    }

    // Board expects data in the format "o:100;u:100;d:100;t:100\n"
    
    public void SaveChanges(ActionEvent e) {
        List<TextField> textFieldsToSave = getAllTextFields(profilePane);
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, "", saveButtonType, ButtonType.CANCEL);
        confirmationAlert.setTitle("Confirm Changes");
        confirmationAlert.setHeaderText("Bạn có chắc chắn muốn lưu tất cả thay đổi?");
        Optional<ButtonType> response = confirmationAlert.showAndWait();

        if (response.isPresent() && response.get() == saveButtonType) {
            for (TextField textField : textFieldsToSave) {
                if (textField.isVisible() && isNumeric(textField.getText())) {
                    double value = Double.parseDouble(textField.getText());
                    boolean isValid = false;
                    String errorText = "";

                    switch (textField.getId()) {
                        case "maxVProText":
                            isValid = value >= 0 && value <= 10;
                            errorText = "Please enter a valid overvoltage value between 0 and 10.";
                            break;
                        case "minVProText":
                            isValid = value >= 0 && value <= 10;
                            errorText = "Please enter a valid undervoltage value between 0 and 10.";
                            break;
                        case "maxTProText":
                            isValid = value >= 0 && value <= 200;
                            errorText = "Please enter a valid temperature protection value between 0 and 200.";
                            break;
                        case "difVProText":
                            isValid = value >= 0 && value <= 1;
                            errorText = "Please enter a valid differential voltage value between 0 and 1.";
                            break;
                        default:
                            isValid = true; // Assume other fields don't have specific validation rules
                            break;
                    }

                    if (isValid) {
                        writeBoard(formatForWrite(textField.getId(), textField.getText()));
                        updateLabel(textField);
                        System.out.println(textField.getText() + " saved.");
                        System.out.println("-----");
                    } else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Invalid Input");
                        errorAlert.setHeaderText("Invalid Number Format");
                        errorAlert.setContentText(errorText);
                        errorAlert.showAndWait();
                    }
                }
            }

            writeBoard("\n");
        } else {
            for (TextField textField : textFieldsToSave) {
                if (textField.isVisible()) {
                    textField.setVisible(false);
                    save.setVisible(false);
                }
            }
        }
    }

    // showSaveConfirmationPopup(text);
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

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    // private String getCorrespondingLabel(String textFieldId) {
    // // Example mapping, replace with actual logic
    // switch (textFieldId) {
    // case "maxVProText":
    // return setting1.getText();
    // case "minVProText":
    // return setting2.getText();
    // case "difVProText":
    // return setting5.getText();
    // case "maxTProText":
    // return setting6.getText();
    // default:
    // return null;
    // }
    // }

    // public void finishEdit(KeyEvent e) {
    // if (e.getCode() == KeyCode.ENTER) {
    // TextField text = (TextField) e.getSource();
    // showSaveConfirmationPopup(text);
    // }
    // else if(e.getCode() == KeyCode.ESCAPE){
    // TextField text = (TextField) e.getSource();
    // text.setVisible(false);
    // save.setVisible(false);
    // }
    // }

    // private void showSaveConfirmationPopup(TextField textField) {
    // // IDs that require numeric input
    // List<String> numericFields = Arrays.asList("maxVProText");
    // if (numericFields.contains(textField.getId()) &&
    // !isNumeric(textField.getText())) {
    // Alert errorAlert = new Alert(Alert.AlertType.ERROR);
    // errorAlert.setTitle("Invalid Input");
    // errorAlert.setHeaderText("Invalid Number Format");
    // errorAlert.setContentText(
    // "Please enter a valid number for \"" +
    // getCorrespondingLabel(textField.getId()) + "\"" + ".");
    // errorAlert.showAndWait();
    // return; // Exit the method early
    // }

    // // Create a custom ButtonType for "Save"
    // ButtonType saveButtonType = new ButtonType("Save",
    // ButtonBar.ButtonData.OK_DONE);
    // Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, "",
    // saveButtonType, ButtonType.CANCEL);
    // confirmationAlert.setTitle("Confirm Changes");
    // confirmationAlert.setHeaderText("Bạn có chắc chắn muốn lưu thay đổi");
    // confirmationAlert.setContentText(getCorrespondingLabel(textField.getId()) + "
    // " + "\"" + textField.getText() + "\"");

    // // Show the alert and wait for response
    // confirmationAlert.showAndWait().ifPresent(response -> {
    // if (response == saveButtonType) {
    // writeBoard(formatAndWriteValue(textField, textField.getText()));
    // updateLabel(textField);
    // save.setVisible(false);
    // } else {
    // textField.setVisible(false); // Hide the text field if not saved
    // save.setVisible(false);
    // }
    // });
    // }

    // // private void popAlert(Alert.AlertType type, String title, String header,
    // String content, TextField textField){
    // // Alert alert = new Alert(type);
    // // alert.setTitle(title);
    // // alert.setHeaderText(header);
    // // alert.setContentText(content);
    // // ButtonType btn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    // // alert.showAndWait().ifPresent(response -> {
    // // if (response == btn) {
    // // writeBoard(formatAndWriteValue(textField, textField.getText()));
    // // updateLabel(textField);
    // // save.setVisible(false);
    // // } else {
    // // textField.setVisible(false); // Hide the text field if not saved
    // // save.setVisible(false);
    // // }
    // // });
    // // }

    private void updateLabel(TextField textField) {

        Platform.runLater(() -> {
            Label targetLabel = null;
            switch (textField.getId()) {
                case "maxVProText":
                    targetLabel = maxVPro;
                    // Assuming numCell is already defined and converted to a numeric value
                    try {
                        double maxVProValue = Double.parseDouble(textField.getText());
                        sumMaxVPro.setText(String.format("%.2f", maxVProValue * numCell));
                        // Corrected this line
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
        });
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


    private String formatForWrite(String id, String inputValue) {
        String output = "";
        double setValue = Double.parseDouble(inputValue);

        switch (id) {
            case "maxVProText":
                output = String.format("o:%d", (int) (setValue * 100));
                break;
            case "minVProText":
                output = String.format("u:%d", (int) (setValue * 100));
                break;
            case "difVProText":
                output = String.format("d:%d", (int) (setValue * 100));
                break;
            case "maxTProText":
                output = String.format("t:%d", (int) (setValue * 10));
                break;
        }

        return output + ";";
    }

    public void setPort(SerialPort port) {
        this.port = port;
    }

    private void writeBoard(String data) {
        System.out.println(data);
        try {
            byte[] bytes = data.getBytes();
            port.writeBytes(bytes, bytes.length);
        } catch (NullPointerException e) {
            System.out.println("No port."); // debug
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void chart(ActionEvent e) throws IOException {
        initCharts(numCell, new Date());
        firstCharts = true;
        runCharts = true;
    }

    private void initCharts(int numCell, Date timestamp) throws IOException {
        chartStage = null;
        ctrlCharts = new Charts(numCell, timestamp);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/Charts.fxml"));
        loader.setController(ctrlCharts);
        Parent root = loader.load();
        Scene chartScene = new Scene(root, Style.CHART_WIDTH, Style.CHART_HEIGHT);

        chartStage = new Stage();
        chartStage.setTitle("Đồ thị trạng thái pin " + getCurrentTimeStamp(timestamp));
        chartStage.setScene(chartScene);
        chartStage.setOnCloseRequest(event -> {
            runCharts = false;
        });
    }

    private CellChart initCellChart(int cell, String timestamp) throws IOException {
        CellChart ctrlCellChart = new CellChart(cell);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/CellChart.fxml"));
        loader.setController(ctrlCellChart);
        Parent root = loader.load();
        Scene chartScene = new Scene(root, Style.CELL_CHART_WIDTH, Style.CELL_CHART_HEIGHT);

        Stage ccStage = new Stage();
        ccStage.setTitle("Đồ thị trạng thái pin " + timestamp);
        ccStage.setScene(chartScene);
        ccStage.setOnCloseRequest(event -> {
            runCellChart.set(cell, false);
        });

        ccStage.show();

        return ctrlCellChart;
    }
}