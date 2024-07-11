import com.fazecast.jSerialComm.SerialPort;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HC05Test {
    public static void main(String[] args) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            SerialPort[] allAvailableComPorts = SerialPort.getCommPorts();
            SerialPort firstAvailableComPort = null;
            for (SerialPort port : allAvailableComPorts) {
                if (port.getDescriptivePortName().contains("BMS")) {
                    firstAvailableComPort = port;
                    break;
                }
            }

            if (firstAvailableComPort == null) {
                System.out.println("No BMS Bluetooth found.");
                System.exit(0);
            } else {
                firstAvailableComPort.openPort();
                firstAvailableComPort.setBaudRate(115200); // Set baud rate to 115200
                firstAvailableComPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
                System.out.println("Opened the BMS port: " + firstAvailableComPort.getDescriptivePortName() + " at 115200 baud.");
            }

            try (Scanner scanner = new Scanner(firstAvailableComPort.getInputStream())) {
                if (!scanner.hasNextLine() || scanner.nextLine() == null) {
                    System.out.println("No data to read.");
                    System.exit(0);
                }
                while (scanner.hasNextLine()) {
                    try {
                        String jsonData = scanner.nextLine(); // Read data from USB

                        System.out.println(jsonData);
                        // if (jsonData == null || jsonData.isEmpty()) {
                        //     System.out.println("No data received. Exiting program.");
                        //     System.exit(0);
                        // } else if (jsonData.equals("END")) {
                        //     System.exit(0);
                        // }
                        // JSONObject jsonObject = new JSONObject(jsonData);
                        // JSONArray dataArray = jsonObject.getJSONArray("data");
                        // for (int i = 0; i < dataArray.length(); i++) {
                        //     JSONObject dataObject = dataArray.getJSONObject(i);
                        //     double voltage = dataObject.optDouble("voltage", Double.NaN);
                        //     double temperature = dataObject.optDouble("temperature", Double.NaN);
                        //     int SOC = dataObject.optInt("SOC", 0);

                        //     if (i == 0) System.out.println("Battery Level: " + SOC + "%");
                        //     System.out.println("Cell " + i + ": " + "Voltage: " + voltage + ", Temperature: " + temperature);
                        // }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        executor.scheduleAtFixedRate(task, 0, 2, TimeUnit.SECONDS);
    }
}