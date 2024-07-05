// Java program to create a labels and textfield and use setLabelFor property
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.image.*;
import java.io.*;
public class label_3 extends Application {

	// launch the application
	public void start(Stage s) throws Exception
	{
		// set title for the stage
		s.setTitle("creating label");

		// TextField
		TextField b1 = new TextField("textfield");

		// create a label
		Label b = new Label("_1 TextField");

		// setlabel for
		b.setLabelFor(b1);

		// TextField
		TextField b4 = new TextField("textfield");

		// create a label
		Label b3 = new Label("_2 TextField");

		// setlabel for
		b3.setLabelFor(b4);

		// create a Tile pane
		TilePane r = new TilePane();

		// setMnemonic
		b.setMnemonicParsing(true);
		b3.setMnemonicParsing(true);

		// add password field
		r.getChildren().add(b);
		r.getChildren().add(b1);
		r.getChildren().add(b3);
		r.getChildren().add(b4);

		// create a scene
		Scene sc = new Scene(r, 200, 200);

		// set the scene
		s.setScene(sc);

		s.show();
	}

	public static void main(String args[])
	{
		// launch the application
		launch(args);
	}
}
