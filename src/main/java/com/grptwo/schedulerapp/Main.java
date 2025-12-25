package com.grptwo.schedulerapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("views/onboarding.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 902, 1255);
        scene.getStylesheets().add(Main.class.getResource("css/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Welcome!");
        stage.show();
    }
}

