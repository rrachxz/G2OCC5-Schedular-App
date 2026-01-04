package com.grptwo.schedulerapp.controllers;

import com.grptwo.schedulerapp.models.Events;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AddController {

    public TextField nameField;
    public TextField descField;
    public DatePicker datePicker;
    public TextField startField;
    public TextField endField;
    public Button createBtn;

    private Events newEvent;
    private Events editingEvent;
    private boolean isEditMode = false;
    private HomepageController homepageController;

    @FXML
    public void initialize() {
        datePicker.setValue(LocalDate.now());
    }

    public void setHomepageController(HomepageController controller) {
        this.homepageController = controller;
    }

    public void setInitialDate(LocalDate date) {
        datePicker.setValue(date);
    }

    public void setEditMode(Events event) {
        isEditMode = true;
        editingEvent = event;

        nameField.setText(event.getTitle());
        descField.setText(event.getDesc());
        datePicker.setValue(event.getStartDateTime().toLocalDate());

        int startHour = event.getStartDateTime().getHour();
        int startMin = event.getStartDateTime().getMinute();
        startField.setText(String.format("%02d:%02d", startHour, startMin));

        int endHour = event.getEndDateTime().getHour();
        int endMin = event.getEndDateTime().getMinute();
        endField.setText(String.format("%02d:%02d", endHour, endMin));

        createBtn.setText("Update Event");
    }

    @FXML
    public void onCreate() {
        if (nameField.getText().isEmpty()) {
            showError("Please enter event name");
            return;
        }

        if (datePicker.getValue() == null) {
            showError("Please select a date");
            return;
        }

        if (startField.getText().isEmpty() || endField.getText().isEmpty()) {
            showError("Please enter start and end times");
            return;
        }

        try {
            String name = nameField.getText();
            String desc = descField.getText();
            LocalDate date = datePicker.getValue();

            String[] startParts = startField.getText().split(":");
            int startHour = Integer.parseInt(startParts[0]);
            int startMin = Integer.parseInt(startParts[1]);

            String[] endParts = endField.getText().split(":");
            int endHour = Integer.parseInt(endParts[0]);
            int endMin = Integer.parseInt(endParts[1]);

            LocalDateTime startDateTime = LocalDateTime.of(date, LocalTime.of(startHour, startMin));
            LocalDateTime endDateTime = LocalDateTime.of(date, LocalTime.of(endHour, endMin));

            if (endDateTime.isBefore(startDateTime) || endDateTime.equals(startDateTime)) {
                showError("End time must be after start time");
                return;
            }

            newEvent = new Events(0, name, desc, startDateTime, endDateTime);

            if (isEditMode) {
                homepageController.updateEvent(editingEvent, newEvent);
            } else {
                homepageController.addEvent(newEvent);
            }

            homepageController.returnToCalendar();

        } catch (Exception e) {
            showError("Invalid time format. Use HH:mm (e.g. 09:00)");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Events getEvent() {
        return newEvent;
    }
}