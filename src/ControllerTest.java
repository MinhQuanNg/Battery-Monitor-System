import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import java.lang.Thread;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import com.fazecast.jSerialComm.SerialPort;
import org.json.JSONArray;
import org.json.JSONException;

public class ControllerTest {
    private Stage stage;
    private Scene scene;
    private Parent root;

    public void up(ActionEvent e) throws IOException {
        SerialPort USB = PortChecker.returnPort();

        if (USB != null) {
            root = FXMLLoader.load(getClass().getResource("ScreenGeneral.fxml"));            
            stage = (Stage)((Node) e.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            
            BlockingQueue<JSONArray> dataQueue = new LinkedBlockingQueue<>();
            DataReader reader = new DataReader(USB, dataQueue);
            Thread thread = new Thread(reader);
            thread.start();
            try {
                processSensorData(dataQueue);
            } catch (JSONException ex) {
                System.out.println(ex);
            }
        } else {
            System.out.println("Error: No USB Serial Ports found.");
        }
    }

    private void processSensorData(BlockingQueue<JSONArray> dataQueue) throws JSONException {
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject dataObject = dataArray.getJSONObject(i);
            dataQueue.put(dataObject); // Add data to the queue
            double voltage = dataObject.optDouble("voltage", Double.NaN);
            double temperature = dataObject.optDouble("temperature", Double.NaN);
            int SOC = dataObject.optInt("SOC", 0);
            System.out.println("Battery Level: " + SOC + "%");
            System.out.println("Cell " + i + ": " + "Voltage: " + voltage + ", Temperature: " + temperature);
            
            //updateDisplay()
        }
    }
}
```