import com.fazecast.jSerialComm.SerialPort;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class DataReader implements Runnable {
    private ControllerGeneral controller;
    private InputStream input;

    public DataReader(ControllerGeneral controller, InputStream input) {
        this.controller = controller;
        this.input = input;
    }

    @Override
    public void run() {
        try (Scanner scanner = new Scanner(input)) {
            scanner.nextLine(); // Skip first line

            // Infinite loop to continuously read data from port
            while (true) {
                String jsonData = scanner.nextLine();
                try {
                    JSONObject jsonObject = new JSONObject(jsonData);
                    JSONArray dataArray = jsonObject.getJSONArray("data");

                    controller.processData(dataArray, new Date());
                } catch (JSONException e) {
                    e.printStackTrace(); // TODO: modal data not formatted as JSON
                } 
            }
        } catch (NoSuchElementException e) {
            System.out.println("No more data.");
        }
    }

    public static void main(String[] args) {
        // SerialPort USB = PortChecker.getPort();

        // // Run if port available
        // if (USB != null) {
        //     DataReader reader = new DataReader(new ControllerGeneral(), USB);
        //     Thread thread = new Thread(reader);
        //     thread.start();
        // } else {
        //     System.out.println("No USB found.");
        // }
    }
}
