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

public class EventsController {

    @FXML private VBox eventsContainer;

    private Map<LocalDate, List<Events>> eventsMap;
    private LocalDate selected;
    private Runnable onUpdate;

    public void init(Map<LocalDate, List<Events>> eventsMap, Runnable onUpdate) {
        this.eventsMap = eventsMap;
        this.onUpdate = onUpdate;
    }

    public void updateEvents(LocalDate date) {
        this.selected = date;
        eventsContainer.getChildren().clear();

        if (!eventsMap.containsKey(selected)) {
            Label noEvents = new Label("No events scheduled");
            noEvents.getStyleClass().add("text-muted");
            noEvents.setStyle("-fx-font-size: 14px; -fx-padding: 20 0 0 0;");
            eventsContainer.getChildren().add(noEvents);
            return;
        }

        List<Events> events = eventsMap.get(selected);
        String[] styles = {"event-primary", "event-secondary", "event-tertiary"};

        for (int i = 0; i < events.size(); i++) {
            eventsContainer.getChildren().add(makeCard(events.get(i), styles[i % 3]));
        }
    }

    private HBox makeCard(Events event, String style) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("event-card");

        VBox content = new VBox(6);
        HBox.setHgrow(content, Priority.ALWAYS);

        HBox timeBox = new HBox(8);
        timeBox.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(4);
        dot.getStyleClass().add("event-dot");
        if (style.equals("event-secondary")) dot.getStyleClass().add("secondary");
        if (style.equals("event-tertiary")) dot.getStyleClass().add("tertiary");

        Label time = new Label(getTime(event.getStartDateTime()) + "-" + getTime(event.getEndDateTime()));
        time.getStyleClass().addAll("event-time", style);
        timeBox.getChildren().addAll(dot, time);

        Label title = new Label(event.getTitle());
        title.getStyleClass().add("event-title");
        title.setWrapText(true);

        HBox descBox = new HBox(5);
        descBox.setAlignment(Pos.CENTER_LEFT);

        Label desc = new Label(event.getDesc());
        desc.getStyleClass().add("event-desc");
        desc.setMaxWidth(180);

        if (event.getDesc().length() > 40) {
            desc.setText(event.getDesc().substring(0, 37) + "...");
            Hyperlink more = new Hyperlink("View more");
            more.getStyleClass().add("event-link");
            more.setOnAction(e -> {
                desc.setText(event.getDesc());
                desc.setWrapText(true);
                more.setVisible(false);
            });
            descBox.getChildren().addAll(desc, more);
        } else {
            descBox.getChildren().add(desc);
        }

        content.getChildren().addAll(timeBox, title, descBox);

        Button btn = new Button("⋮");
        btn.getStyleClass().add("event-more-btn");
        btn.setOnAction(e -> showOptions(event, btn));

        card.getChildren().addAll(content, btn);
        return card;
    }

    private void showOptions(Events event, Button btn) {
        ContextMenu menu = new ContextMenu();
        MenuItem edit = new MenuItem("Edit");
        MenuItem delete = new MenuItem("Delete");

        edit.setOnAction(e -> editEvent(event));
        delete.setOnAction(e -> deleteEvent(event));

        menu.getItems().addAll(edit, delete);
        menu.show(btn, 0, btn.getHeight());
    }

    private void editEvent(Events event) {
        System.out.println("editing: " + event.getTitle());
    }

    private void deleteEvent(Events event) {
        for (Map.Entry<LocalDate, List<Events>> entry : eventsMap.entrySet()) {
            List<Events> list = entry.getValue();
            if (list.contains(event)) {
                list.remove(event);
                if (list.isEmpty()) eventsMap.remove(entry.getKey());
                break;
            }
        }
        updateEvents(selected);
        if (onUpdate != null) onUpdate.run();
    }

    private String getTime(LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}