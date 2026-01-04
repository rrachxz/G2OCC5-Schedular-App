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
    public TextArea noteArea;
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
        if (date != null) {
            datePicker.setValue(date);
        }
    }

    public void setEditMode(Events event) {
        this.isEditMode = true;
        this.editingEvent = event;

        nameField.setText(event.getTitle());

        String desc = event.getDesc();
        if (desc != null && !desc.isEmpty()) {
            descField.setText(desc);
            noteArea.setText(desc);
        }

        if (event.getStartDateTime() != null) {
            datePicker.setValue(event.getStartDateTime().toLocalDate());
            startField.setText(formatTime(event.getStartDateTime().toLocalTime()));
        }

        if (event.getEndDateTime() != null) {
            endField.setText(formatTime(event.getEndDateTime().toLocalTime()));
        }

        createBtn.setText("Update Event");
    }

    @FXML
    public void onCreate() {
        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            showAlert("Please enter event name");
            return;
        }

        if (datePicker.getValue() == null) {
            showAlert("Please select a date");
            return;
        }

        String startTimeStr = startField.getText();
        String endTimeStr = endField.getText();

        if (startTimeStr == null || startTimeStr.trim().isEmpty()) {
            showAlert("Please enter start time");
            return;
        }

        if (endTimeStr == null || endTimeStr.trim().isEmpty()) {
            showAlert("Please enter end time");
            return;
        }

        try {
            LocalDate date = datePicker.getValue();
            LocalTime startTime = parseTime(startTimeStr);
            LocalTime endTime = parseTime(endTimeStr);

            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                showAlert("End time must be after start time");
                return;
            }

            LocalDateTime startDt = LocalDateTime.of(date, startTime);
            LocalDateTime endDt = LocalDateTime.of(date, endTime);

            String description = descField.getText();
            if (description == null) {
                description = "";
            }

            newEvent = new Events(0, name.trim(), description.trim(), startDt, endDt);

            if (homepageController != null) {
                if (isEditMode && editingEvent != null) {
                    homepageController.updateEvent(editingEvent, newEvent);
                } else {
                    homepageController.addEvent(newEvent);
                }
                homepageController.returnToCalendar();
            }

        } catch (NumberFormatException e) {
            showAlert("Invalid time format. Please use HH:mm format (e.g., 09:00, 14:30)");
        } catch (Exception e) {
            showAlert("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private LocalTime parseTime(String timeStr) throws NumberFormatException {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            throw new NumberFormatException("Time is empty");
        }

        timeStr = timeStr.trim();

        if (!timeStr.contains(":")) {
            throw new NumberFormatException("Time must contain ':'");
        }

        String[] parts = timeStr.split(":");

        if (parts.length != 2) {
            throw new NumberFormatException("Invalid time format");
        }

        int hours = Integer.parseInt(parts[0].trim());
        int minutes = Integer.parseInt(parts[1].trim());

        if (hours < 0 || hours > 23) {
            throw new NumberFormatException("Hours must be 0-23");
        }

        if (minutes < 0 || minutes > 59) {
            throw new NumberFormatException("Minutes must be 0-59");
        }

        return LocalTime.of(hours, minutes);
    }

    private String formatTime(LocalTime time) {
        if (time == null) return "";
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Events getEvent() {
        return newEvent;
    }
}