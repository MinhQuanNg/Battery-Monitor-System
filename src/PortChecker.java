import java.util.Scanner;
import com.fazecast.jSerialComm.SerialPort;

public class PortChecker {
    public static SerialPort getPort() {
        SerialPort[] allAvailableComPorts = SerialPort.getCommPorts();
        SerialPort USB = null;
        for (SerialPort port : allAvailableComPorts) {

            if (port.getDescriptivePortName().toLowerCase().contains("usb")) {
                USB = port;
                USB.openPort();
                USB.setBaudRate(115200); // Set baud rate to 115200
                USB.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
                System.out.println("Opened the USB serial port: " + USB.getDescriptivePortName());

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