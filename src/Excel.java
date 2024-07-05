import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Excel {
    File fileName;
    String sheetName;

    public Excel() throws IOException {
        fileName = new File("src/data.xlsx");
        sheetName = "Data";

        XSSFWorkbook workbook = new XSSFWorkbook();

        // Save the workbook
        FileOutputStream outputStream = new FileOutputStream(fileName);
        workbook.write(outputStream);
        outputStream.close();
        workbook.close();
        System.out.println("XLSX file created successfully.");
    }

    public void write(JSONArray recordsToWrite, String timestamp, Hashtable<String, Double> maxmin) throws FileNotFoundException, IOException, JSONException {
        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(fileName));
        XSSFSheet sheet = workbook.getSheet(sheetName);
        int lastRow = sheet.getLastRowNum();

        // If not row 0, then jump 2 rows (leaving 1 row space)
        int rowNum = lastRow == 0 ? 0 : lastRow + 2;

        int currRowNum = rowNum;

        // Set headers in column 0
        // TODO: pull headers from dictionary
        Row row = sheet.createRow(currRowNum++);
        Cell cell = row.createCell(0);
        cell.setCellValue(timestamp);

        row = sheet.createRow(currRowNum++);
        cell = row.createCell(0);
        cell.setCellValue("Voltage (V)");

        row = sheet.createRow(currRowNum);
        cell = row.createCell(0);
        cell.setCellValue("Temperature (C)");

        Map<Integer, Object[]> data = prepareData(rowNum, recordsToWrite);

        Set<Integer> keySet = data.keySet();

        // Cell / row
        // for (Integer key : keySet) {
        //     row = sheet.createRow(rowNum++);
        //     Object[] objArr = data.get(key);
        //     int cellNum = 0;
        //     for (Object obj : objArr) {
        //         cell = row.createCell(cellNum++);
        //         if (obj instanceof String) {
        //             cell.setCellValue((String) obj);
        //         } else if (obj instanceof Integer) {
        //             cell.setCellValue((Integer) obj);
        //         } else if (obj instanceof Double) {
        //             cell.setCellValue((Double) obj);
        //         }
        //     }
        // }

        // Cell / column
        for (Integer key : keySet) {
            int cellNum = 1;
            currRowNum = rowNum;

            Object[] objArr = data.get(key);
            for (Object obj : objArr) {
                row = sheet.getRow(currRowNum);
                cell = row.createCell(cellNum);

                XSSFCellStyle style = workbook.createCellStyle();
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                if (currRowNum == rowNum + 1) {
                    // highlight maxV
                    if ((Double) obj == maxmin.get("maxV")) {
                        style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
                        cell.setCellStyle(style);
                    }
                    
                    // highlight minV
                    else if ((Double) obj == maxmin.get("minV")) {
                        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
                        cell.setCellStyle(style);
                    }

                    // add sumV after last column
                    if (cellNum == objArr.length) {
                        cell = row.createCell(cellNum++);
                        cell.setCellValue((Double) maxmin.get("sumV"));
                    }
                }

                else if (currRowNum == rowNum + 2) {
                    // highlight maxT
                    if ((Double) obj == maxmin.get("maxT")) {
                        style.setFillForegroundColor(IndexedColors.RED.getIndex());
                        cell.setCellStyle(style);
                    }
                }

                if (obj instanceof String) {
                    cell.setCellValue((String) obj);
                } else if (obj instanceof Integer) {
                    cell.setCellValue((Integer) obj);
                } else if (obj instanceof Double) {
                    cell.setCellValue((Double) obj);
                }
                currRowNum++;
            }
            
            cellNum++;
        }


        
        try {
            FileOutputStream out = new FileOutputStream(fileName);
            workbook.write(out);

            out.close();
            workbook.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
    }

    private static Map<Integer, Object[]> prepareData(int rowNum,
                                                    JSONArray dataArray) throws JSONException {
        Map<Integer, Object[]> data = new HashMap<>();
        for (int i = 1; i <= dataArray.length(); i++) {
            JSONObject dataObject = dataArray.getJSONObject(i-1);

            double voltage = dataObject.optDouble("voltage", Double.NaN);
            double temperature = dataObject.optDouble("temperature", Double.NaN);

            rowNum++;
            data.put(rowNum, new Object[]{i, voltage, temperature});
        }
        return data;
    }

    public static void main(String[] args) {
        try {
            Excel excel = new Excel();
            JSONArray dataArray = new JSONArray();

            // Using javax.json
            JsonObject jsonObject1 = Json.createObjectBuilder()
                .add("voltage", 3.35)
                .add("temperature", 29.1)
                .build();

            dataArray.put(jsonObject1);

            excel.write(dataArray, "10:42", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
