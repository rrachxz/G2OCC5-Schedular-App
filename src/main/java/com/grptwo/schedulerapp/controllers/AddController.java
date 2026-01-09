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

public class AddController {

    @FXML private TextField nameField;
    @FXML private TextField descField;
    @FXML private DatePicker datePicker;
    @FXML private TextField startField;
    @FXML private TextField endField;
    @FXML private Button createBtn;

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

        // event details
        nameField.setText(event.getTitle());
        descField.setText(event.getDesc());
        datePicker.setValue(event.getStartDateTime().toLocalDate());

        //times
        startField.setText(String.format("%02d:%02d",
                event.getStartDateTime().getHour(),
                event.getStartDateTime().getMinute()));
        endField.setText(String.format("%02d:%02d",
                event.getEndDateTime().getHour(),
                event.getEndDateTime().getMinute()));

        createBtn.setText("Update Event");

        // repeat settings
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
        // check if fields are filled
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
            // get event info
            String name = nameField.getText();
            String desc = descField.getText();
            LocalDate date = datePicker.getValue();

            // parse times
            String[] start = startField.getText().split(":");
            String[] end = endField.getText().split(":");

            LocalDateTime startTime = LocalDateTime.of(date,
                    LocalTime.of(Integer.parseInt(start[0]), Integer.parseInt(start[1])));
            LocalDateTime endTime = LocalDateTime.of(date,
                    LocalTime.of(Integer.parseInt(end[0]), Integer.parseInt(end[1])));

            // validate times
            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                showError("End time must be after start time");
                return;
            }

            // get repeat settings
            Recurrance repeat = getRepeatSettings();

            // create event
            newEvent = new Events(0, name, desc, startTime, endTime, repeat);

            // save event
            if (isEditMode) {
                homepageController.updateEvent(editingEvent, newEvent);
            } else {
                if (repeat != null) {
                    // create multipe events
                    List<Events> allEvents = makeRepeatingEvents(newEvent);
                    for (Events e : allEvents) {
                        homepageController.addEvent(e);
                    }
                } else {
                    // create single event
                    homepageController.addEvent(newEvent);
                }
            }

            homepageController.returnToCalendar();

        } catch (Exception e) {
            showError("Invalid time. Use HH:mm like 09:00");
        }
    }

    // get repeat settings from form
    private Recurrance getRepeatSettings() {
        if (noRepeatRadio.isSelected()) {
            return null;
        }

        // which repeat type
        int days = 0;
        if (dailyRadio.isSelected()) days = 1;
        if (weeklyRadio.isSelected()) days = 7;
        if (monthlyRadio.isSelected()) days = 30;

        // how many times
        Integer times = null;
        if (!timesField.getText().isEmpty()) {
            try {
                times = Integer.parseInt(timesField.getText());
            } catch (Exception e) {
                times = null;
            }
        }

        // until when?
        LocalDateTime until = null;
        if (endDatePicker.getValue() != null) {
            until = endDatePicker.getValue().atTime(23, 59);
        }

        return new Recurrance(0, days, times, until);
    }

    // make multiple repeating events
    private List<Events> makeRepeatingEvents(Events event) {
        List<Events> events = new ArrayList<>();
        Recurrance repeat = event.getRecurrence();

        LocalDateTime start = event.getStartDateTime();
        LocalDateTime end = event.getEndDateTime();
        long minutes = java.time.Duration.between(start, end).toMinutes();

        // how many to create?
        int maxEvents = 50;
        LocalDateTime stopDate = start.plusYears(1);

        if (repeat.getEndDateTime() != null) {
            stopDate = repeat.getEndDateTime();
            maxEvents = 365;
        } else if (repeat.getTimes() != null) {
            maxEvents = repeat.getTimes();
            stopDate = start.plusYears(2);
        }

        // create events
        int count = 0;
        while (count < maxEvents && start.isBefore(stopDate)) {
            Events newEvent = new Events(
                    event.getId() + count,
                    event.getTitle(),
                    event.getDesc(),
                    start,
                    end,
                    repeat
            );

            events.add(newEvent);

            // Next date
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

    public Events getEvent() {
        return newEvent;
    }
}