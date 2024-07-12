import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortIOException;

public class PortChecker {
    public static List<SerialPort> getPorts() {
        SerialPort[] allAvailableComPorts = SerialPort.getCommPorts();
        List<SerialPort> filteredPorts = new ArrayList<>();
        for (SerialPort port : allAvailableComPorts) {
            if ((port.getDescriptivePortName().contains("USB") || port.getDescriptivePortName().contains("BMS")) && 
                !port.getDescriptivePortName().toLowerCase().contains("dial-in")) {
                filteredPorts.add(port);
            }
        }
        return filteredPorts;
    }

    public static InputStream preparePort(SerialPort port) throws Exception {
        int baud = 115200;

        if (port == null) {
            throw new IllegalArgumentException("Port is null.");
        }

        port.openPort();
        port.setBaudRate(baud);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        System.out.println("Opened the port " + port.getDescriptivePortName() + " at " + baud + " baud.");

        try (InputStream inputStream = port.getInputStream()) {
            if (inputStream.available() == 0) {
                throw new Exception("No data to read.");
            }

            return inputStream;
        } catch (SerialPortIOException e) {
            throw new SerialPortIOException("SerialPortIOException handled.");
        } catch (NullPointerException e) {
            throw new NullPointerException("NullPointerException handled.");
        }
    }
}