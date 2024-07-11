import com.fazecast.jSerialComm.SerialPort;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class DataReader implements Runnable {
    private SerialPort port;
    private ControllerGeneral controller;

    public DataReader(ControllerGeneral controller, SerialPort port) {
        this.port = port;
        this.controller = controller;
    }

    @Override
    public void run() {
        try (InputStream inputStream = port.getInputStream()) {
            if (inputStream.available() == 0) {
                System.out.println("No data to read.");
                System.exit(0); // TODO: modal
            }

            try (Scanner scanner = new Scanner(inputStream)) {
                // Infinite loop to continuously read data from port
                while (!scanner.nextLine().equals("END")) {
                    String jsonData = scanner.nextLine();
                    if (jsonData == null || jsonData.isEmpty()) {
                        System.out.println("No data received. Exiting program.");
                        System.exit(0);
                    } else {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        JSONArray dataArray = jsonObject.getJSONArray("data");

                        controller.processData(dataArray, new Date());
                        Thread.sleep(1000); // Read every 1s
                    }
                }
            }
        } catch (InterruptedException e) {
            System.exit(0);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            System.out.println("No more data.");
            System.exit(0);
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
