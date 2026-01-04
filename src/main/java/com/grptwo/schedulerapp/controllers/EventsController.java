package com.grptwo.schedulerapp.controllers;

import com.grptwo.schedulerapp.models.Events;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class EventsController {

    @FXML private VBox eventsContainer;

    private Map<LocalDate, List<Events>> eventsMap;
    private LocalDate selectedDate;
    private Runnable onUpdate;
    private Consumer<Events> onEdit;

    public void init(Map<LocalDate, List<Events>> eventsMap, Runnable onUpdate, Consumer<Events> onEdit) {
        this.eventsMap = eventsMap;
        this.onUpdate = onUpdate;
        this.onEdit = onEdit;
    }

    public void updateEvents(LocalDate date) {
        selectedDate = date;
        eventsContainer.getChildren().clear();

        if (!eventsMap.containsKey(selectedDate) || eventsMap.get(selectedDate).isEmpty()) {
            Label noEvents = new Label("No events scheduled");
            noEvents.getStyleClass().add("text-muted");
            noEvents.setStyle("-fx-font-size: 14px; -fx-padding: 20 0 0 0;");
            eventsContainer.getChildren().add(noEvents);
            return;
        }

        List<Events> events = eventsMap.get(selectedDate);
        String[] colors = {"event-primary", "event-secondary", "event-tertiary"};

        for (int i = 0; i < events.size(); i++) {
            HBox eventCard = createEventCard(events.get(i), colors[i % 3]);
            eventsContainer.getChildren().add(eventCard);
        }
    }

    private HBox createEventCard(Events event, String colorStyle) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("event-card");

        VBox content = new VBox(6);
        HBox.setHgrow(content, Priority.ALWAYS);

        HBox timeSection = new HBox(8);
        timeSection.setAlignment(Pos.CENTER_LEFT);

        Circle colorDot = new Circle(4);
        colorDot.getStyleClass().add("event-dot");
        if (colorStyle.equals("event-secondary")) {
            colorDot.getStyleClass().add("secondary");
        }
        if (colorStyle.equals("event-tertiary")) {
            colorDot.getStyleClass().add("tertiary");
        }

        String startTime = formatTime(event.getStartDateTime());
        String endTime = formatTime(event.getEndDateTime());
        Label timeLabel = new Label(startTime + " - " + endTime);
        timeLabel.getStyleClass().addAll("event-time", colorStyle);

        timeSection.getChildren().addAll(colorDot, timeLabel);

        Label titleLabel = new Label(event.getTitle());
        titleLabel.getStyleClass().add("event-title");
        titleLabel.setWrapText(true);

        String description = event.getDesc();
        if (description != null && !description.isEmpty()) {
            Label descLabel = new Label(description);
            descLabel.getStyleClass().add("event-desc");
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(200);
            content.getChildren().addAll(timeSection, titleLabel, descLabel);
        } else {
            content.getChildren().addAll(timeSection, titleLabel);
        }

        Button menuBtn = new Button("⋮");
        menuBtn.getStyleClass().add("event-more-btn");
        menuBtn.setOnAction(e -> showMenu(event, menuBtn));

        card.getChildren().addAll(content, menuBtn);
        return card;
    }

    private void showMenu(Events event, Button button) {
        ContextMenu menu = new ContextMenu();

        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> {
            if (onEdit != null) {
                onEdit.accept(event);
            }
        });

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> deleteConfirm(event));

        menu.getItems().addAll(editItem, deleteItem);
        menu.show(button, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void deleteConfirm(Events event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Event");
        alert.setHeaderText("Delete \"" + event.getTitle() + "\"?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteEvent(event);
        }
    }

    private void deleteEvent(Events event) {
        for (Map.Entry<LocalDate, List<Events>> entry : eventsMap.entrySet()) {
            List<Events> eventsList = entry.getValue();

            if (eventsList.contains(event)) {
                eventsList.remove(event);

                if (eventsList.isEmpty()) {
                    eventsMap.remove(entry.getKey());
                }
                break;
            }
        }

        updateEvents(selectedDate);

        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    private String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "00:00";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return dateTime.format(formatter);
    }
}