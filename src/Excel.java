import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Excel {
    public Excel() {
    String fileName = "your_file_name.xlsx"; // Replace with your desired name

    // Check if the file already exists
    File excelFile = new File(fileName);
    if (!excelFile.exists()) {
      // Create a new workbook (XLSX format)
      XSSFWorkbook workbook = new XSSFWorkbook();

      // Write the workbook to the file (creates the file if it doesn't exist)
      FileOutputStream outputStream = new FileOutputStream(excelFile);
      workbook.write(outputStream);
      outputStream.close();
      System.out.println("Excel file created: " + fileName);
    } else {
      System.out.println("Excel file already exists: " + fileName);
    }
    }
}
