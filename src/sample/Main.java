package sample;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    @FXML
    private static BorderPane contentArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        setUpGUI(primaryStage);
    }

    private void setUpGUI(Stage primaryStage) throws java.io.IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("EditorGUI.fxml"));
        fxmlLoader.setRoot(contentArea);
        primaryStage.setTitle("Subtitles Editor");
        primaryStage.setScene(new Scene(fxmlLoader.load(), 550, 350));
        primaryStage.show();
    }
}
