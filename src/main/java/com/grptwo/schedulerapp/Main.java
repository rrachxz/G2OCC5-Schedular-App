package com.grptwo.schedulerapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.scene.Parent;
import javafx.stage.StageStyle;
public class Main extends Application {
    private double xOffset = 0;
    private double yOffset = 0;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("views/onboarding.fxml"));
        Parent root = fxmlLoader.load();
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
        Scene scene = new Scene(root, 902, 1255);
        scene.getStylesheets().add(Main.class.getResource("css/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Welcome!");
        stage.show();
    }
}

