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

    @FXML
    protected void onContinueButtonClick(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) continueBtn.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/com/grptwo/schedulerapp/views/homepage.fxml")
            );

            Scene scene = new Scene(fxmlLoader.load(), 902, 1255);
            stage.setScene(scene);
            stage.setTitle("G2OCC5 Scheduler App");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}