import java.util.ArrayList;
import java.util.List;

import com.fazecast.jSerialComm.SerialPort;

public class PortChecker {
    public static List<SerialPort> getPorts() {
        SerialPort[] allAvailableComPorts = SerialPort.getCommPorts();
        List<SerialPort> filteredPorts = new ArrayList<>();
        for (SerialPort port : allAvailableComPorts) {
            if (!port.getDescriptivePortName().toLowerCase().contains("dial-in")) {
            filteredPorts.add(port);
            }
        }
        return filteredPorts;
    }

    public static boolean preparePort(SerialPort port) {
        if (port.getDescriptivePortName().toLowerCase().contains("bms")) {
            port.openPort();
            port.setBaudRate(115200); // Set baud rate to 115200
            port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
            System.out.println("Opened the serial port: " + port.getDescriptivePortName() + " at 115200 baud.");
            return true;
        } else if (port.getDescriptivePortName().toLowerCase().contains("usb")) {
            return true;
        } else {
            return false;
        }
    }
}