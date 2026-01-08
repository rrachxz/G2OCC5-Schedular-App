package com.grptwo.schedulerapp.controllers;

import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.application.Platform; // a new import so that we can initialize the size of window in Controller file

public class Controller {
    @FXML
    private Button continueBtn;

    //This sets the size when the app opens
    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            Stage stage = (Stage) continueBtn.getScene().getWindow();
            stage.setWidth(1000);
            stage.setHeight(700);
            stage.centerOnScreen();
        });
    }

    @FXML
    protected void onContinueButtonClick(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) continueBtn.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/grptwo/schedulerapp/views/homepage.fxml"));

            Scene scene = new Scene(fxmlLoader.load());

            stage.setScene(scene);

            // This sets the size for the next page(Calendar page)
            stage.setWidth(800);
            stage.setHeight(900);
            stage.centerOnScreen();
            stage.setTitle("G2OCC5 Scheduler App");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}