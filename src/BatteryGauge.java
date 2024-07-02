import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Gauge.SkinType;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.Section;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.fxml.FXML;

public class BatteryGauge extends Pane {
    @FXML private Gauge gauge;

    public BatteryGauge(int percent) {
        Gauge gauge = GaugeBuilder.create()
                    .skinType(SkinType.BATTERY)
                    .animated(true)
                    .sectionsVisible(true)
                    .sections(new Section(0, 10, Color.RED),
                                new Section(10, 20, Color.rgb(255,235,59)), //YELLOW
                                new Section(20, 100, Color.GREEN))
                    .build();
        
                    gauge.setValue(percent);

        Pane pane = new Pane();
        pane.getChildren().add(gauge);
    }
}
