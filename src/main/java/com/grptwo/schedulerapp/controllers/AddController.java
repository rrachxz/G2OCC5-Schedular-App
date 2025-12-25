package com.grptwo.schedulerapp.controllers;

import com.grptwo.schedulerapp.models.Events;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AddController {

    public TextField nameField;
    public TextField descField;
    public TextArea noteArea;
    public DatePicker datePicker;
    public TextField startField;
    public TextField endField;
    public ToggleButton remindToggle;
    public Button createBtn;

    private Events newEvent;

    @FXML
    public void initialize() {
        datePicker.setValue(LocalDate.now());
    }

    @FXML
    public void onCreate() {
        if (nameField.getText().isEmpty()) {
            showAlert("Please enter event name");
            return;
        }

        if (datePicker.getValue() == null) {
            showAlert("Please select a date");
            return;
        }

        try {
            String name = nameField.getText();
            String note = noteArea.getText();
            LocalDate date = datePicker.getValue();

            LocalTime start = parseTime(startField.getText());
            LocalTime end = parseTime(endField.getText());

            LocalDateTime startDt = date.atTime(start);
            LocalDateTime endDt = date.atTime(end);

            newEvent = new Events(0, name, note, startDt, endDt);

            closeWindow();
        } catch (Exception e) {
            showAlert("Invalid time format. Use HH:mm");
        }
    }

    private LocalTime parseTime(String time) {
        if (time.isEmpty()) return LocalTime.of(0, 0);
        String[] parts = time.split(":");
        return LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) createBtn.getScene().getWindow();
        stage.close();
    }

    public Events getEvent() {
        return newEvent;
    }
}

