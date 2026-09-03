package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneController {

    @FXML
    private BorderPane mainPane;
    
    @FXML
    private TextArea consoleTextArea;
    
    @FXML
    public void initialize() throws IOException {
        if (consoleTextArea != null) {
            consoleTextArea.appendText("Application started \n");
        }
        changeToFiltering();
    }
    
    public void addToConsole(String message) {
        if (consoleTextArea != null) {
            consoleTextArea.appendText(message);
        }
    }

    public void changeToFiltering() throws IOException {
        loadView("/resources/fxml/filteringScene.fxml");
    }

    public void changeToProcessing() throws IOException {
        loadView("/resources/fxml/processingScene.fxml");
    }

    public void changeToAnalyzing() throws IOException {
        loadView("/resources/fxml/analyzingScene.fxml");
    }

    public void changeToCalculating() throws IOException {
        loadView("/resources/fxml/calculatingScene.fxml");
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            javafx.scene.Node content = loader.load();

            Object controller = loader.getController();
            if (controller instanceof FilteringController) {
                ((FilteringController) controller).setMainController(this);
            }
            if (controller instanceof ProcessingController) {
                ((ProcessingController) controller).setMainController(this);
            }
            if (controller instanceof AnalyzingController) {
                ((AnalyzingController) controller).setMainController(this);
            }
            if (controller instanceof CalculatingController) {
                ((CalculatingController) controller).setMainController(this);
            }
            
            mainPane.setCenter(content);
        } catch (IOException e) {
            addToConsole("Error loading view: " + fxmlFile);
            e.printStackTrace();
        }
    }

    public void exitApplication() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("Check if any of the programs are still running");
        alert.setContentText("Are you sure you want to exit the app?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            Stage stage = (Stage) mainPane.getScene().getWindow();
            stage.close();
        }
    }
}