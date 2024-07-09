import com.fazecast.jSerialComm.SerialPort;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class DataReader implements Runnable {
    private SerialPort USB;
    private ControllerGeneral controller;

    public DataReader(ControllerGeneral controller, SerialPort USB) {
        this.USB = USB;
        this.controller = controller;
    }

    @Override
    public void run() {
        try (Scanner scanner = new Scanner(USB.getInputStream())) {
            if (!scanner.hasNextLine() || scanner.nextLine() == null) {
                System.out.println("No data to read.");
                System.exit(0);
            }

            // Infinite loop to continuously read data from USB
            while (scanner.nextLine() != "END") {
                    String jsonData = scanner.nextLine();
                    if (jsonData == null || jsonData.isEmpty()) {
                        System.out.println("No data received. Exiting program.");
                        System.exit(0);
                    } else {
                    JSONObject jsonObject = new JSONObject(jsonData);
                    JSONArray dataArray = jsonObject.getJSONArray("data");

                    controller.processData(dataArray, getCurrentTimeStamp());
                    Thread.sleep(1000); // Read every 1s
                }
            }
        } catch (InterruptedException e) {
            System.exit(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }          
    }

    public static void main(String[] args) {
        SerialPort USB = PortChecker.getPort();

        // Run if port available
        if (USB != null) {
            DataReader reader = new DataReader(new ControllerGeneral(), USB);
            Thread thread = new Thread(reader);
            thread.start();
        } else {
            System.out.println("No USB found.");
        }
    }

    private static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
}