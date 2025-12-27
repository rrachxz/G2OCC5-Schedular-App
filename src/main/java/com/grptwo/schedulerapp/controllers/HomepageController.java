package com.grptwo.schedulerapp.controllers;

import com.grptwo.schedulerapp.models.Events;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.*;

public class HomepageController {

    public VBox navbar;
    public VBox calendar;
    public ScrollPane events;

    @FXML private CalendarController calendarController;
    @FXML private EventsController eventsController;

    private final Map<LocalDate, List<Events>> eventsMap = new HashMap<>();

    @FXML
    public void initialize() {
        loadEvents();

        calendarController.init(eventsMap, this::onDateChange);
        eventsController.init(eventsMap, this::onEventsUpdate);

        eventsController.updateEvents(calendarController.getSelected());
    }

    private void loadEvents() {
        LocalDate today = LocalDate.now();

        Events e1 = new Events(1, "go to cinema with rara", "we're gonna watch avengers doomsday",
                today.atTime(10, 0), today.atTime(13, 0));
        Events e2 = new Events(2, "cooking with keisha", "learn how to cook a fried rice",
                today.atTime(10, 0), today.atTime(13, 0));
        Events e3 = new Events(3, "studying with liz", "learn abt math and cso",
                today.atTime(10, 0), today.atTime(13, 0));

        eventsMap.put(today, Arrays.asList(e1, e2, e3));
        eventsMap.put(today.plusDays(1), Arrays.asList(e1));
        eventsMap.put(today.plusDays(2), Arrays.asList(e1, e2));
        eventsMap.put(today.plusDays(4), Arrays.asList(e2));
        eventsMap.put(today.plusDays(8), Arrays.asList(e1));
        eventsMap.put(today.plusDays(9), Arrays.asList(e1, e2, e3));
    }

    private void onDateChange() {
        eventsController.updateEvents(calendarController.getSelected());
    }

    private void onEventsUpdate() {
        calendarController.updateCal();
    }

    public void addEvent(Events event) {
        LocalDate date = event.getStartDateTime().toLocalDate();
        if (!eventsMap.containsKey(date)) {
            eventsMap.put(date, new ArrayList<>());
        }
        eventsMap.get(date).add(event);
        onEventsUpdate();
        eventsController.updateEvents(calendarController.getSelected());
    }
}