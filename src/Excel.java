import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javafx.stage.FileChooser;

public class Excel {
    File file;

    public Excel() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Data");

        String[] headers = {"Timestamp", "Cell", "Voltage", "Temperature"};
        Row row = sheet.createRow(0);
        int cellNum = 0;
        for (String header : headers) {
            Cell cell = row.createCell(cellNum++);
            cell.setCellValue(header);
        }

        // Save the workbook
        FileOutputStream outputStream = new FileOutputStream("data.xlsx");
        workbook.write(outputStream);
        outputStream.close();
        workbook.close();
        System.out.println("XLSX file created successfully.");
    }

    public void write(JSONArray recordsToWrite, String timestamp) throws FileNotFoundException, IOException, JSONException {
        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));
        Sheet sheet = workbook.getSheetAt(0);
        int rowNum = sheet.getLastRowNum() + 1;

        Map<Integer, Object[]> data = prepareData(rowNum, recordsToWrite, timestamp);

        Set<Integer> keySet = data.keySet();
        for (Integer key : keySet) {
            Row row = sheet.createRow(rowNum++);
            Object[] objArr = data.get(key);
            int cellNum = 0;
            for (Object obj : objArr) {
                Cell cell = row.createCell(cellNum++);
                if (obj instanceof String) {
                    cell.setCellValue((String) obj);
                } else if (obj instanceof Integer) {
                    cell.setCellValue((Integer) obj);
                } else if (obj instanceof Double) {
                    cell.setCellValue((Double) obj);
                }
            }
        }
        
        try {
            FileOutputStream out = new FileOutputStream(file);
            workbook.write(out);

            out.close();
            workbook.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
    }

    private static Map<Integer, Object[]> prepareData(int rowNum,
                                                    JSONArray dataArray, String timestamp) throws JSONException {
        Map<Integer, Object[]> data = new HashMap<>();
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject dataObject = dataArray.getJSONObject(i);

            double voltage = dataObject.optDouble("voltage", Double.NaN);
            double temperature = dataObject.optDouble("temperature", Double.NaN);

            rowNum++;
            data.put(rowNum, new Object[]{timestamp, i, voltage, temperature});
        }
        return data;
    }

    public void save() throws FileNotFoundException, IOException {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showSaveDialog(null);

        if (selectedFile != null) {
            // Proceed with saving the Excel file to the selected path
            XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));
  
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

    public static void main(String[] args) {
        // try {
        // Excel excel = new Excel();
        // JSONArray dataArray = new JSONArray();

        // // Using javax.json
        // JsonObject jsonObject1 = Json.createObjectBuilder()
        //     .add("voltage", 3.35)
        //     .add("temperature", 29.1)
        //     .build();

        // dataArray.put(jsonObject1); // org.json

        // excel.write(dataArray, "10:42");
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }

        //create a workbook
Workbook workbook = new XSSFWorkbook();

//create a sheet in the workbook(you can give it a name)
Sheet sheet = workbook.createSheet("excel-sheet");

//create a row in the sheet
Row row = sheet.createRow(0);

//add cells in the sheet
Cell cell = row.createCell(0);

//set a value to the cell
cell.setCellValue("something");

//save the Excel file
try {
    FileOutputStream out = new FileOutputStream(
            new File("excel.xlsx"));
    workbook.write(out);
    out.close();
} catch (Exception e) {
    e.printStackTrace();
}
    }
}
