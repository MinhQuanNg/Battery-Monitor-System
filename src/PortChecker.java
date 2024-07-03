import com.fazecast.jSerialComm.SerialPort;

public class PortChecker {
    public static boolean checkPort() {
        SerialPort[] allAvailableComPorts = SerialPort.getCommPorts();
        for (SerialPort port : allAvailableComPorts) {
            if (port.getDescriptivePortName().toLowerCase().contains("usb")) {
                return true;
            }
        }
        return false;
    }
    public static SerialPort returnPort() {
        SerialPort[] allAvailableComPorts = SerialPort.getCommPorts();
        SerialPort USBDetection = null;
        for (SerialPort port : allAvailableComPorts) {
            if (port.getDescriptivePortName().toLowerCase().contains("usb")) {
                USBDetection = port;
                break;
            }
        }
        if (USBDetection == null) {
            System.out.println("No USB Serial Ports found.");
            System.exit(0);
        } else {
            USBDetection.openPort();
            USBDetection.setBaudRate(115200); // Set baud rate to 115200
            USBDetection.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
            System.out.println("Opened the USB serial port: " + USBDetection.getDescriptivePortName() + " at 115200 baud.");
        }
        return USBDetection;
    }
}