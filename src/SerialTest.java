import com.fazecast.jSerialComm.SerialPort;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SerialTest {
    public static void main(String[] args) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            SerialPort[] allAvailableComPorts = SerialPort.getCommPorts();
            SerialPort firstAvailableComPort = null;
            for (SerialPort port : allAvailableComPorts) {
                if (port.getDescriptivePortName().toLowerCase().contains("usb")) {
                    firstAvailableComPort = port;
                    break;
                }
            }
            if (firstAvailableComPort == null) {
                System.out.println("No USB Serial Ports found.");
                System.exit(0);
            } else {
                firstAvailableComPort.openPort();
                firstAvailableComPort.setBaudRate(115200); // Set baud rate to 115200
                firstAvailableComPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
                System.out.println("Opened the USB serial port: " + firstAvailableComPort.getDescriptivePortName() + " at 115200 baud.");
            }
            try (Scanner scanner = new Scanner(firstAvailableComPort.getInputStream())) {
                while (scanner.hasNextLine()) {
                    try {
                        String jsonData = scanner.nextLine(); // Read data from USB
                        System.out.println(jsonData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                System.out.println("No data to read.");
                System.exit(0);
            }
        };
        executor.scheduleAtFixedRate(task, 0, 2, TimeUnit.SECONDS);
    }
}