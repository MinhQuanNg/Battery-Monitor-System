import com.fazecast.jSerialComm.SerialPort;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

public class DataReader implements Runnable {
    private SerialPort USB;
    private BlockingQueue<JSONArray> dataQueue; // Assuming you're using a thread-safe queue

    public DataReader(SerialPort USB, BlockingQueue<JSONArray> dataQueue) {
        this.USB = USB;
        this.dataQueue = dataQueue;
    }

    @Override
    public void run() {
        try (Scanner scanner = new Scanner(USB.getInputStream())) {
            if (!scanner.hasNextLine() || scanner.nextLine() == null) {
                System.out.println("No data to read.");
                System.exit(0);
            }
            while (scanner.nextLine() != "END") { // Infinite loop to continuously read data
                    String jsonData = scanner.nextLine(); // Read data from USB
                    if (jsonData == null || jsonData.isEmpty()) {
                        System.out.println("No data received. Exiting program.");
                        System.exit(0);
                    } else {
                    JSONObject jsonObject = new JSONObject(jsonData);
                    JSONArray dataArray = jsonObject.getJSONArray("data");
                    processSensorData(dataArray); // Call a separate method to process data
                    Thread.sleep(2000);
                }
        } catch (Exception e) {
            e.printStackTrace(); // Pause for 2 seconds              
        }          
    }

    private void processSensorData(JSONArray dataArray) throws JSONException {
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject dataObject = dataArray.getJSONObject(i);
            double voltage = dataObject.optDouble("voltage", Double.NaN);
            double temperature = dataObject.optDouble("temperature", Double.NaN);
            int SOC = dataObject.optInt("SOC", 0);
            System.out.println("Battery Level: " + SOC + "%");
            System.out.println("Cell " + i + ": " + "Voltage: " + voltage + ", Temperature: " + temperature);
        }
    }
    
}