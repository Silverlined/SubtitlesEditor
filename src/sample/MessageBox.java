package sample;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

final class MessageBox {

    private Stage primaryStage;

    private MessageBox(Stage stage) {
        primaryStage = stage;
        primaryStage.initModality(Modality.APPLICATION_MODAL);
        primaryStage.setTitle("SubtitlesEditor");
        primaryStage.setMinWidth(200);
        primaryStage.setMinHeight(40);
    }

    static void display(String message) {
        MessageBox messageBox = new MessageBox(new Stage());

        Label label = new Label(message);

        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().add(label);

        Scene scene = new Scene(layout, 200, 40);

        messageBox.primaryStage.setScene(scene);
        messageBox.primaryStage.resizableProperty().setValue(false);
        messageBox.primaryStage.showAndWait();
    }
}
