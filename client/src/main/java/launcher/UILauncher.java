package launcher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import javafx.stage.Stage;

import java.net.URL;

public class UILauncher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL fxmlUrl = getClass().getClassLoader().getResource("views/CalculatorUI.fxml");
        Pane root = FXMLLoader.<Pane>load(fxmlUrl);

        Scene scene = new Scene(root, 500, 875);
        primaryStage.setTitle("Calculator RMI");
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image("img/sk.jpg"));
        primaryStage.setOnCloseRequest(we -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}

