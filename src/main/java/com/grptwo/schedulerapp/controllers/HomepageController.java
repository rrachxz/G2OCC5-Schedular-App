package com.grptwo.schedulerapp.controllers;

import com.grptwo.schedulerapp.models.Events;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
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
        eventsController.init(eventsMap, this::onEventsUpdate, this::openEditPage);

        navbarController.setHomepageController(this);

        eventsController.updateEvents(calendarController.getSelected());
    }

    private void onDateChange() {
        eventsController.updateEvents(calendarController.getSelected());
    }

    private void onEventsUpdate() {
        calendarController.updateCal();
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

        int maxId = eventsMap.values().stream()
                .flatMap(List::stream)
                .mapToInt(Events::getId)
                .max()
                .orElse(0);
        event.setId(maxId + 1);

        eventsMap.get(date).add(event);

        onEventsUpdate();
        eventsController.updateEvents(calendarController.getSelected());
    }

    public void updateEvent(Events oldEvent, Events newEvent) {
        if (oldEvent == null || newEvent == null) return;

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

        newEvent.setId(oldEvent.getId());
        LocalDate date = newEvent.getStartDateTime().toLocalDate();

        if (!eventsMap.containsKey(date)) {
            eventsMap.put(date, new ArrayList<>());
        }
        eventsMap.get(date).add(newEvent);

        onEventsUpdate();
        eventsController.updateEvents(calendarController.getSelected());
    }

    public void returnToCalendar() {
        navbarController.onCalendarClick();
    }
}