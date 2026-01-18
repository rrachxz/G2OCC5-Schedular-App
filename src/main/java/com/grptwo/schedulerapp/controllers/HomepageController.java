package com.grptwo.schedulerapp.controllers;

import com.grptwo.schedulerapp.models.Events;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class HomepageController {

    public VBox navbar;
    public VBox calendar;

    @FXML private CalendarController calendarController;
    @FXML private EventsController eventsController;
    @FXML private NavbarController navbarController;

    private final Map<LocalDate, List<Events>> eventsMap = new HashMap<>();

    // UPDATED: Path to your persistent storage file
    private final String SAVE_FILE = "data/events.csv";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    @FXML
    public void initialize() {
        loadDataOnStartup();

        calendarController.init(eventsMap, this::onDateChange);
        calendarController.setOnAddEventRequest(this::openAddPage);
        eventsController.init(eventsMap, this::onEventsUpdate, this::openEditPage);
        navbarController.setHomepageController(this);
        eventsController.updateEvents(calendarController.getSelected());

        // Trigger the notification check when the app launches
        Platform.runLater(this::checkNextEventOnLaunch);
    }
    private void loadDataOnStartup() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine(); // Skip header line

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",");
                if (p.length >= 6) {
                    int id = Integer.parseInt(p[0]);
                    String title = p[1];
                    String desc = p[2];
                    LocalDateTime start = LocalDateTime.parse(p[3], formatter);
                    LocalDateTime end = LocalDateTime.parse(p[4], formatter);
                    int reminder = Integer.parseInt(p[5]);

                    Events event = new Events(id, title, desc, start, end, null, reminder);
                    LocalDate date = start.toLocalDate();
                    eventsMap.computeIfAbsent(date, k -> new ArrayList<>()).add(event);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load events: " + e.getMessage());
        }
    }

    private void saveData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_FILE))) {
            writer.write("id,title,description,startDateTime,endDateTime,reminder");
            writer.newLine();

            for (List<Events> list : eventsMap.values()) {
                for (Events event : list) {
                    String line = String.format("%d,%s,%s,%s,%s,%d",
                            event.getId(),
                            escapeCsv(event.getTitle()),
                            escapeCsv(event.getDesc()),
                            event.getStartDateTime().format(formatter),
                            event.getEndDateTime().format(formatter),
                            event.getReminderMinutes()
                    );
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        return val.replace(",", " "); // Simple escape to prevent breaking CSV columns
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
        saveData();
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
        saveData(); // Auto Save
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
        saveData();
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

    // event statistics
    public void showStatistics() {
        int totalUpcoming = 0;
        int totalThisWeek = 0;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfWeek = now.plusDays(7);

        // Map to count events per day of the week
        Map<java.time.DayOfWeek, Integer> dayCounts = new HashMap<>();
        for (java.time.DayOfWeek day : java.time.DayOfWeek.values()) {
            dayCounts.put(day, 0);
        }
        boolean hasAnyEvents = !eventsMap.isEmpty();

        for (Map.Entry<LocalDate, List<Events>> entry : eventsMap.entrySet()) {
            LocalDate date = entry.getKey();
            List<Events> events = entry.getValue();
            int count = events.size();

            // 1. Count Upcoming (any event today or in future)
            if (!date.isBefore(now.toLocalDate())) {
                totalUpcoming += count;
            }

            // 2. Count for "This Week" (next 7 days)
            if (!date.isBefore(now.toLocalDate()) && date.isBefore(endOfWeek.toLocalDate())) {
                totalThisWeek += count;
            }

            // 3. Count for Busiest Day
            java.time.DayOfWeek dayOfWeek = date.getDayOfWeek();
            dayCounts.put(dayOfWeek, dayCounts.get(dayOfWeek) + count);
        }

        // Find the busiest day
        String busiestDayResult;
        // Check if any events were actually counted in the dayCounts map
        int totalEventCount = dayCounts.values().stream().mapToInt(Integer::intValue).sum();

        if (totalEventCount == 0) {
            busiestDayResult = "-";
        } else {
            java.time.DayOfWeek busiestDay = dayCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .get().getKey();
            busiestDayResult = busiestDay.toString() + " (" + dayCounts.get(busiestDay) + " events)";
        }

        // Display the results
        Alert statsAlert = new Alert(Alert.AlertType.INFORMATION);
        statsAlert.setTitle("Event Statistics");
        statsAlert.setHeaderText("Your Calendar Insights");
        statsAlert.setContentText(
                "Total Upcoming Events: " + totalUpcoming +
                        "\nEvents in the next 7 days: " + totalThisWeek +
                        "\nBusiest Day of the Week: " + busiestDayResult
        );
        statsAlert.showAndWait();
    }
}