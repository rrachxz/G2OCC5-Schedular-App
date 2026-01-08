package com.grptwo.schedulerapp.controllers;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class Controller {
    @FXML
    private Button continueBtn;

    // Inside Controller.java

    @FXML
    protected void onContinueButtonClick(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) continueBtn.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/grptwo/schedulerapp/views/homepage.fxml"));

            Scene scene = new Scene(fxmlLoader.load());

            stage.setScene(scene);

            stage.setWidth(900);
            stage.setHeight(800);

            //Center the new window on the screen so it looks nice
            stage.centerOnScreen();

            stage.setTitle("G2OCC5 Scheduler App");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}