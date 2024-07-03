import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataControllerTest {
    private static JSONArray dataArray;

    public static void setData(JSONArray dataArray) throws JSONException {
        DataControllerTest.dataArray = dataArray;
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject dataObject = dataArray.getJSONObject(i);
            double voltage = dataObject.optDouble("voltage", Double.NaN);
            double temperature = dataObject.optDouble("temperature", Double.NaN);
            int SOC = dataObject.optInt("SOC", 0);
            System.out.println("Battery Level: " + SOC + "%");
            System.out.println("Cell " + i + ": " + "Voltage: " + voltage + ", Temperature: " + temperature);
        }
    }

    public void processData() throws JSONException {
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
