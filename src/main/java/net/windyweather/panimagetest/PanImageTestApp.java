package net.windyweather.panimagetest;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;



public class PanImageTestApp extends Application {

    PanImageTestController ssCtrl;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(PanImageTestApp.class.getResource("panimage-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Pan Image Test");
        stage.setScene(scene);
        stage.show();

        // get the controller so we can call it with window events
        ssCtrl = fxmlLoader.getController();
        ssCtrl.setUpStuff();
    }

    public static void main(String[] args) {
        launch();
    }
}