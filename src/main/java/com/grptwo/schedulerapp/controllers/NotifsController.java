package com.grptwo.schedulerapp.controllers;

import com.grptwo.schedulerapp.models.Events;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotifsController {

    @FXML private VBox notifsContainer;

    private Map<LocalDate, List<Events>> eventsMap;
    private List<NotifItem> notifs;

    @FXML
    public void initialize() {
    }

    public void init(Map<LocalDate, List<Events>> eventsMap) {
        this.eventsMap = eventsMap;
        this.notifs = new ArrayList<>();
        refresh();
    }

    public void refresh() {
        generateNotifs();
        displayNotifs();
    }

    private void generateNotifs() {
        notifs.clear();
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        // check events for the next 7 days
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);
            if (eventsMap.containsKey(date)) {
                for (Events event : eventsMap.get(date)) {
                    LocalDateTime eventStart = event.getStartDateTime();

                    // calculate time until event
                    long minsUntil = ChronoUnit.MINUTES.between(now, eventStart);
                    long hoursUntil = ChronoUnit.HOURS.between(now, eventStart);
                    long daysUntil = ChronoUnit.DAYS.between(now.toLocalDate(), eventStart.toLocalDate());

                    String message;
                    if (minsUntil < 0) {
                        continue;
                    } else if (minsUntil < 60) {
                        if (minsUntil == 0) {
                            message = "Starting now!";
                        } else if (minsUntil == 1) {
                            message = "Starting in 1 minute";
                        } else {
                            message = "Starting in " + minsUntil + " minutes";
                        }
                    } else if (hoursUntil < 24) {
                        if (hoursUntil == 1) {
                            message = "Starting in 1 hour";
                        } else {
                            message = "Starting in " + hoursUntil + " hours";
                        }
                    } else if (daysUntil == 1) {
                        message = "Tomorrow at " + eventStart.format(DateTimeFormatter.ofPattern("HH:mm"));
                    } else if (daysUntil <= 7) {
                        message = "In " + daysUntil + " days at " + eventStart.format(DateTimeFormatter.ofPattern("HH:mm"));
                    } else {
                        continue;
                    }

                    notifs.add(new NotifItem(event, message, eventStart));
                }
            }
        }
        notifs.sort((a, b) -> a.eventTime.compareTo(b.eventTime));
    }

    private void displayNotifs() {
        notifsContainer.getChildren().clear();

        if (notifs.isEmpty()) {
            Label noNotifs = new Label("No upcoming events");
            noNotifs.getStyleClass().add("text-muted");
            noNotifs.setStyle("-fx-font-size: 16px; -fx-padding: 40 0 0 0;");
            notifsContainer.getChildren().add(noNotifs);
            return;
        }

        for (NotifItem notif : notifs) {
            notifsContainer.getChildren().add(createNotifCard(notif));
        }
    }

    private VBox createNotifCard(NotifItem notif) {
        VBox card = new VBox(10);
        card.getStyleClass().add("notif-card");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(6);
        dot.getStyleClass().add("notif-dot");

        Label typeLabel = new Label("Reminder");
        typeLabel.getStyleClass().add("notif-type");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("×");
        deleteBtn.getStyleClass().add("notif-delete-btn");
        deleteBtn.setOnAction(e -> {
            notifs.remove(notif);
            displayNotifs();
        });

        header.getChildren().addAll(dot, typeLabel, spacer, deleteBtn);

        Label title = new Label(notif.event.getTitle());
        title.getStyleClass().add("notif-title");
        title.setWrapText(true);

        Label time = new Label(notif.message);
        time.getStyleClass().add("notif-time");

        Label date = new Label(notif.eventTime.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        date.getStyleClass().add("notif-date");

        card.getChildren().addAll(header, title, time, date);

        return card;
    }

    private static class NotifItem {
        Events event;
        String message;
        LocalDateTime eventTime;

        NotifItem(Events event, String message, LocalDateTime eventTime) {
            this.event = event;
            this.message = message;
            this.eventTime = eventTime;
        }
    }
}