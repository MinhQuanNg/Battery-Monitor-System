import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fazecast.jSerialComm.SerialPort;

public class PortChecker {
    public static SerialPort getPort() {
        SerialPort[] allAvailableComPorts = SerialPort.getCommPorts();
        SerialPort USB = null;
        for (SerialPort port : allAvailableComPorts) {
            System.out.println("hi");

            if (port.getDescriptivePortName().toLowerCase().contains("usb")) {
                USB = port;
                USB.openPort();
                USB.setBaudRate(115200); // Set baud rate to 115200
                USB.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
                System.out.println("Opened the USB serial port: " + USB.getDescriptivePortName() + " at 115200 baud.");
            //     try (Scanner scanner = new Scanner(USBDetection.getInputStream())) {
            //     if (!scanner.hasNextLine() || scanner.nextLine() == null) {
            //         System.out.println("No data to read.");
            //         System.exit(0);
            //     }
            //     while (scanner.hasNextLine()) {
            //         try {
            //             String jsonData = scanner.nextLine(); // Read data from USB
            //             if (jsonData == null || jsonData.isEmpty()) {
            //                 System.out.println("No data received. Exiting program.");
            //                 System.exit(0);
            //             }
            //             JSONObject jsonObject = new JSONObject(jsonData);
            //             JSONArray dataArray = jsonObject.getJSONArray("data");
            //             for (int i = 0; i < dataArray.length(); i++) {
            //                 JSONObject dataObject = dataArray.getJSONObject(i);
            //                 double voltage = dataObject.optDouble("voltage", Double.NaN);
            //                 double temperature = dataObject.optDouble("temperature", Double.NaN);
            //                 int SOC = dataObject.optInt("SOC", 0);
            //                 if (i == 0) {
            //                     System.out.println("Overview: Voltage: " + voltage + ", Temperature: " + temperature + ", Battery Level: " + SOC + "%");
            //                 }
            //             }
            //         } catch (Exception e) {
            //             e.printStackTrace();
            //         }
            //     }
            // } finally {
            //     USBDetection.closePort();
            // }

                try (Scanner scanner = new Scanner(USB.getInputStream())) {
                    if (scanner.hasNextLine()) {
                        break;
                    }
                }
            }
        }

        return USB;
    }
}