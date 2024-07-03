import com.fazecast.jSerialComm.SerialPort;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Test {
    public static void main(String[] args) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            SerialPort[] allAvailableComPorts = SerialPort.getCommPorts();
            SerialPort firstAvailableComPort = null;
            for (SerialPort port : allAvailableComPorts) {
                System.out.println("hi");
    
                if (port.getDescriptivePortName().toLowerCase().contains("usb")) {
                    firstAvailableComPort = port;
                    firstAvailableComPort.openPort();
                    firstAvailableComPort.setBaudRate(115200); // Set baud rate to 115200
                    firstAvailableComPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
                    System.out.println("Opened the USB serial port: " + firstAvailableComPort.getDescriptivePortName() + " at 115200 baud.");
                    try (Scanner scanner = new Scanner(firstAvailableComPort.getInputStream())) {
                        if (scanner.hasNextLine()) {
                            break;
                        }
                    }
                }
            }
            try (Scanner scanner = new Scanner(firstAvailableComPort.getInputStream())) {
                if (!scanner.hasNextLine() || scanner.nextLine() == null) {
                    System.out.println("No data to read.");
                    System.exit(0);
                }
                while (scanner.hasNextLine()) {
                    try {
                        String jsonData = scanner.nextLine(); // Read data from USB
                        if (jsonData == null || jsonData.isEmpty()) {
                            System.out.println("No data received. Exiting program.");
                            System.exit(0);
                        }
                        JSONObject jsonObject = new JSONObject(jsonData);
                        JSONArray dataArray = jsonObject.getJSONArray("data");
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject dataObject = dataArray.getJSONObject(i);
                            double voltage = dataObject.optDouble("voltage", Double.NaN);
                            double temperature = dataObject.optDouble("temperature", Double.NaN);
                            int SOC = dataObject.optInt("SOC", 0);
                            if (i == 0) {
                                System.out.println("Overview: Voltage: " + voltage + ", Temperature: " + temperature + ", Battery Level: " + SOC + "%");
                            }

                            else {
                            System.out.println("Cell " + i + ": " + "Voltage: " + voltage + ", Temperature: " + temperature); }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                firstAvailableComPort.closePort();
            }
        };
        executor.scheduleAtFixedRate(task, 0, 2, TimeUnit.SECONDS);
    }
}