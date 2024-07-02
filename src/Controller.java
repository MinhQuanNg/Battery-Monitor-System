import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Gauge.SkinType;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.Section;

public class Controller {
    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML private Pane batteryPane;
    private Gauge gauge;

    public void up(ActionEvent e) throws IOException {
        root = FXMLLoader.load(getClass().getResource("ScreenGeneral.fxml"));            
        stage = (Stage)((Node) e.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        gauge = GaugeBuilder.create()
                    .skinType(SkinType.BATTERY)
                    .animated(true)
                    .sectionsVisible(true)
                    .sections(new Section(0, 10, Color.RED),
                                new Section(10, 20, Color.rgb(255,235,59)), //YELLOW
                                new Section(20, 100, Color.GREEN))
                    .build();
        
                    gauge.setValue(50);

                    // batteryPane.getChildren().add(gauge);
    }

    public void initialize() {
    }
}
