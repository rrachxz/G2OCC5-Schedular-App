package com.grptwo.schedulerapp.controllers;

import com.grptwo.schedulerapp.models.Events;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class HomepageController {

    public VBox navbar;
    public VBox calendar;

    @FXML private CalendarController calendarController;
    @FXML private EventsController eventsController;
    @FXML private NavbarController navbarController;

    private final Map<LocalDate, List<Events>> eventsMap = new HashMap<>();

    @FXML
    public void initialize() {
        calendarController.init(eventsMap, this::onDateChange);
        calendarController.setOnAddEventRequest(this::openAddPage);
        eventsController.init(eventsMap, this::onEventsUpdate, this::openEditPage);
        navbarController.setHomepageController(this);
        eventsController.updateEvents(calendarController.getSelected());

        // Trigger the notification check when the app launches
        Platform.runLater(this::checkNextEventOnLaunch);
    }

    // [UPDATED METHOD]: Checks for events based on the user's specific reminder setting
    private void checkNextEventOnLaunch() {
        LocalDateTime now = LocalDateTime.now();
        Events nextEvent = null;
        long minMinutes = Long.MAX_VALUE;

        // Iterate through all events
        for (List<Events> eventList : eventsMap.values()) {
            for (Events event : eventList) {

                // 1. Get the reminder time set by the user (e.g., 30)
                int reminderThreshold = event.getReminderMinutes();

                // If user selected "None" (0) or invalid, skip
                if (reminderThreshold <= 0) continue;

                // 2. Calculate time until event starts
                long minutesUntil = ChronoUnit.MINUTES.between(now, event.getStartDateTime());

                // 3. Logic:
                // minutesUntil > 0: Event is in the future
                // minutesUntil <= reminderThreshold: We are inside the reminder window
                // (e.g., Event is in 10 mins, Threshold is 30 mins -> 10 <= 30 -> Show Alert)
                if (minutesUntil > 0 && minutesUntil <= reminderThreshold) {

                    // If multiple events match, pick the one starting soonest
                    if (minutesUntil < minMinutes) {
                        minMinutes = minutesUntil;
                        nextEvent = event;
                    }
                }
            }
        }

        // Show the Alert if an event was found
        if (nextEvent != null) {
            String durationString;
            if (minMinutes < 60) {
                durationString = minMinutes + " minutes";
            } else {
                long hours = minMinutes / 60;
                long mins = minMinutes % 60;
                durationString = hours + " hours" + (mins > 0 ? " " + mins + " minutes" : "");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Reminder");
            alert.setHeaderText("Upcoming Event");
            alert.setContentText("Your event '" + nextEvent.getTitle() + "' is starting in " + durationString +
                    "\n(You set a reminder for " + getReminderLabel(nextEvent.getReminderMinutes()) + ")");
            alert.show();
        }
    }

    // Helper to make the alert message look nice
    private String getReminderLabel(int minutes) {
        if (minutes == 15) return "15 minutes before";
        if (minutes == 30) return "30 minutes before";
        if (minutes == 60) return "1 hour before";
        if (minutes == 1440) return "1 day before";
        return minutes + " minutes before";
    }

    private void onDateChange() {
        eventsController.updateEvents(calendarController.getSelected());
    }

    public void onEventsUpdate() {
        calendarController.updateCal();
        eventsController.updateEvents(calendarController.getSelected());
    }

    public void openAddPage() {
        navbarController.loadAddView(calendarController.getSelected(), null);
    }

    public void openEditPage(Events event) {
        navbarController.loadAddView(calendarController.getSelected(), event);
    }

    public void addEvent(Events event) {
        if (event == null) return;

        LocalDate date = event.getStartDateTime().toLocalDate();

        if (!eventsMap.containsKey(date)) {
            eventsMap.put(date, new ArrayList<>());
        }

        // Generate ID
        int maxId = eventsMap.values().stream()
                .flatMap(List::stream)
                .mapToInt(Events::getId)
                .max()
                .orElse(0);
        event.setId(maxId + 1);

        // Add event to map
        eventsMap.get(date).add(event);

        // Update UI
        onEventsUpdate();
        eventsController.updateEvents(calendarController.getSelected());
    }

    public void updateEvent(Events oldEvent, Events newEvent) {
        if (oldEvent == null || newEvent == null) return;

        // Remove old event
        for (Map.Entry<LocalDate, List<Events>> entry : eventsMap.entrySet()) {
            List<Events> list = entry.getValue();
            if (list.contains(oldEvent)) {
                list.remove(oldEvent);
                if (list.isEmpty()) {
                    eventsMap.remove(entry.getKey());
                }
                break;
            }
        }

        // Setup new event ID and date
        newEvent.setId(oldEvent.getId());
        LocalDate date = newEvent.getStartDateTime().toLocalDate();

        if (!eventsMap.containsKey(date)) {
            eventsMap.put(date, new ArrayList<>());
        }

        // Add new event
        eventsMap.get(date).add(newEvent);

        // Update UI
        onEventsUpdate();
        eventsController.updateEvents(calendarController.getSelected());
    }

    public void returnToCalendar() {
        navbarController.onCalendarClick();
    }

    public Map<LocalDate, List<Events>> getEventsMap() {
        return eventsMap;
    }
}