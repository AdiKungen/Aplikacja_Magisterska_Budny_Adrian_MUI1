package application;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ArrangingController {
	
	@FXML
	ChoiceBox<String> choiceId;
	@FXML
	ChoiceBox<String> choiceTime;
	@FXML
	ChoiceBox<String> choiceType;
	
	@FXML
	Label lineLabel;
	
	@FXML
	Button shuffleButton;
	
	private String[] newParametry;
	
	public String[] getNewParametry() {
		return newParametry;
	}
	
	public void setChoice(String line, String[] parametry) {
		lineLabel.setText(line);
		choiceId.getItems().addAll(parametry);
		choiceTime.getItems().addAll(parametry);
		choiceType.getItems().addAll(parametry);
	}
	
	public void submit() throws IOException {
		String[] tempParametry = {choiceId.getValue(), choiceTime.getValue(), choiceType.getValue()};
		newParametry = tempParametry;
		Stage stage = (Stage) choiceId.getScene().getWindow();
		stage.close();
	}
	
}
