package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/mainScene.fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.setScene(scene);
            primaryStage.setTitle("Adrian Budny Praca Magisterska");
            String css = this.getClass().getResource("/resources/css/application.css").toExternalForm();
            scene.getStylesheets().add(css);
            primaryStage.getIcons().add(new Image(getClass().getResource("/resources/icon/icon.png").toExternalForm()));
            primaryStage.show();

            primaryStage.setOnCloseRequest(event -> {
                event.consume();
                SceneController controller = loader.getController();
                controller.exitApplication();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}