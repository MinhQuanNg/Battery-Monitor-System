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
        int baud = 115200;

        if (port == null) {
            return false;
        }

        if (port.getDescriptivePortName().toLowerCase().contains("usb")
        || port.getDescriptivePortName().toLowerCase().contains("bms")) {
            port.openPort();
            port.setBaudRate(baud);
            port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
            System.out.println("Opened the port " + port.getDescriptivePortName() + " at " + baud + " baud.");
            return true;
        } else {
            return false;
        }
    }
}