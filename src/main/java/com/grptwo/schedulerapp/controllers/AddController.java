package com.grptwo.schedulerapp.controllers;

import com.grptwo.schedulerapp.models.Events;
import com.grptwo.schedulerapp.models.Recurrance;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AddController {

    @FXML private TextField nameField;
    @FXML private TextField descField;
    @FXML private DatePicker datePicker;
    @FXML private TextField startField;
    @FXML private TextField endField;
    @FXML private Button createBtn;

    @FXML private ComboBox<String> reminderBox;

    @FXML private RadioButton noRepeatRadio;
    @FXML private RadioButton dailyRadio;
    @FXML private RadioButton weeklyRadio;
    @FXML private RadioButton monthlyRadio;
    @FXML private VBox recurrenceOptionsBox;
    @FXML private TextField timesField;
    @FXML private DatePicker endDatePicker;

    private ToggleGroup repeatGroup;
    private Events newEvent;
    private Events editingEvent;
    private boolean isEditMode = false;
    private HomepageController homepageController;

    @FXML
    public void initialize() {
        datePicker.setValue(LocalDate.now());

        repeatGroup = new ToggleGroup();
        noRepeatRadio.setToggleGroup(repeatGroup);
        dailyRadio.setToggleGroup(repeatGroup);
        weeklyRadio.setToggleGroup(repeatGroup);
        monthlyRadio.setToggleGroup(repeatGroup);

        repeatGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
            boolean showOptions = (newVal != noRepeatRadio);
            recurrenceOptionsBox.setVisible(showOptions);
            recurrenceOptionsBox.setManaged(showOptions);
        });

        // Initialize reminder options
        if (reminderBox != null) {
            reminderBox.getItems().addAll(
                    "None",
                    "15 minutes before",
                    "30 minutes before",
                    "1 hour before",
                    "1 day before"
            );
            reminderBox.getSelectionModel().selectFirst();
        }
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

        // Populate existing information
        nameField.setText(event.getTitle());
        descField.setText(event.getDesc());
        datePicker.setValue(event.getStartDateTime().toLocalDate());

        startField.setText(String.format("%02d:%02d",
                event.getStartDateTime().getHour(),
                event.getStartDateTime().getMinute()));
        endField.setText(String.format("%02d:%02d",
                event.getEndDateTime().getHour(),
                event.getEndDateTime().getMinute()));

        createBtn.setText("Update Event");

        // Set reminder selection (restore state)
        setReminderSelection(event.getReminderMinutes());

        // Set recurrence options (restore state)
        if (event.getRecurrence() != null) {
            Recurrance rec = event.getRecurrence();

            if (rec.getInterval() == 1) dailyRadio.setSelected(true);
            else if (rec.getInterval() == 7) weeklyRadio.setSelected(true);
            else if (rec.getInterval() == 30) monthlyRadio.setSelected(true);

            if (rec.getTimes() != null) {
                timesField.setText(rec.getTimes().toString());
            }
            if (rec.getEndDateTime() != null) {
                endDatePicker.setValue(rec.getEndDateTime().toLocalDate());
            }
        }
    }

    @FXML
    public void onCreate() {
        // Simple validation
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
            // Get basic info
            String name = nameField.getText();
            String desc = descField.getText();
            LocalDate date = datePicker.getValue();

            // Parse time
            String[] start = startField.getText().split(":");
            String[] end = endField.getText().split(":");

            LocalDateTime startTime = LocalDateTime.of(date,
                    LocalTime.of(Integer.parseInt(start[0]), Integer.parseInt(start[1])));
            LocalDateTime endTime = LocalDateTime.of(date,
                    LocalTime.of(Integer.parseInt(end[0]), Integer.parseInt(end[1])));

            // Time validation logic
            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                showError("End time must be after start time");
                return;
            }

            // Get recurrence settings
            Recurrance repeat = getRepeatSettings();

            // Get reminder settings
            int reminderMins = getReminderMinutes();

            // Create new event object (using new constructor with reminder)
            newEvent = new Events(0, name, desc, startTime, endTime, repeat, reminderMins);

            // conflict detection
            boolean hasConflict = false;
            String conflictDetails = "";

            List<Events> eventsToCheck = new ArrayList<>();
            if (repeat != null) {
                eventsToCheck = makeRepeatingEvents(newEvent);
            } else {
                eventsToCheck.add(newEvent);
            }
            for (Events pEvent : eventsToCheck) {
                LocalDate checkDate = pEvent.getStartDateTime().toLocalDate();
                List<Events> eventsOnThatDay = homepageController.getEventsMap().get(checkDate);

                if (eventsOnThatDay != null) {
                    for (Events e : eventsOnThatDay) {
                        if (isEditMode && editingEvent != null && e.getId() == editingEvent.getId()) {
                            continue;
                        }
                        if (pEvent.getStartDateTime().isBefore(e.getEndDateTime()) &&
                                pEvent.getEndDateTime().isAfter(e.getStartDateTime())) {
                            hasConflict = true;
                            showError("Time clash detected on " + checkDate + "!\nOverlaps with: " + e.getTitle());
                            break;
                        }
                    }
                }
                if (hasConflict) break;
            }
            if (hasConflict) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Conflict Warning");
                confirm.setHeaderText("Time Clash Detected!");
                confirm.setContentText(conflictDetails + "Do you want to add this event anyway?");

                Optional<ButtonType> result = confirm.showAndWait();
                if (result.isEmpty() || result.get() != ButtonType.OK) {
                    return; // User cancelled or clicked No, so stop here
                }
            }

            // Save logic
            if (isEditMode) {
                homepageController.updateEvent(editingEvent, newEvent);
            } else {
                if (repeat != null) {
                    // Create multiple repeating events
                    List<Events> allEvents = makeRepeatingEvents(newEvent);
                    for (Events e : allEvents) {
                        homepageController.addEvent(e);
                    }
                } else {
                    // Create single event
                    homepageController.addEvent(newEvent);
                }
            }

            homepageController.returnToCalendar();

        } catch (Exception e) {
            showError("Invalid time. Use HH:mm like 09:00");
            e.printStackTrace();
        }
    }

    // Helper method: Get minutes from ComboBox
    private int getReminderMinutes() {
        if (reminderBox == null) return 0;
        String val = reminderBox.getValue();
        if (val == null || val.equals("None")) return 0;
        if (val.contains("15")) return 15;
        if (val.contains("30")) return 30;
        if (val.contains("1 hour")) return 60;
        if (val.contains("1 day")) return 1440;
        return 0;
    }

    private void setReminderSelection(int minutes) {
        if (reminderBox == null) return;
        if (minutes == 15) reminderBox.setValue("15 minutes before");
        else if (minutes == 30) reminderBox.setValue("30 minutes before");
        else if (minutes == 60) reminderBox.setValue("1 hour before");
        else if (minutes == 1440) reminderBox.setValue("1 day before");
        else reminderBox.setValue("None");
    }

    private Recurrance getRepeatSettings() {
        if (noRepeatRadio.isSelected()) {
            return null;
        }

        int days = 0;
        if (dailyRadio.isSelected()) days = 1;
        if (weeklyRadio.isSelected()) days = 7;
        if (monthlyRadio.isSelected()) days = 30;

        Integer times = null;
        if (!timesField.getText().isEmpty()) {
            try {
                times = Integer.parseInt(timesField.getText());
            } catch (Exception e) {
                times = null;
            }
        }

        LocalDateTime until = null;
        if (endDatePicker.getValue() != null) {
            until = endDatePicker.getValue().atTime(23, 59);
        }

        return new Recurrance(0, days, times, until);
    }

    private List<Events> makeRepeatingEvents(Events event) {
        List<Events> events = new ArrayList<>();
        Recurrance repeat = event.getRecurrence();

        LocalDateTime start = event.getStartDateTime();
        LocalDateTime end = event.getEndDateTime();
        long minutes = java.time.Duration.between(start, end).toMinutes();

        int maxEvents = 5;
        LocalDateTime stopDate = start.plusYears(1);

        if (repeat.getEndDateTime() != null) {
            stopDate = repeat.getEndDateTime();
            maxEvents = 365;
        } else if (repeat.getTimes() != null) {
            maxEvents = repeat.getTimes();
            stopDate = start.plusYears(2);
        }

        int count = 0;
        while (count < maxEvents && start.isBefore(stopDate)) {
            // Ensure reminderMinutes is included when copying events
            Events newEvent = new Events(
                    event.getId() + count,
                    event.getTitle(),
                    event.getDesc(),
                    start,
                    end,
                    repeat,
                    event.getReminderMinutes() // Ensure reminder time is copied
            );

            events.add(newEvent);

            start = start.plusDays(repeat.getInterval());
            end = start.plusMinutes(minutes);
            count++;
        }

        return events;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setContentText(message);
        alert.showAndWait();
    }
}