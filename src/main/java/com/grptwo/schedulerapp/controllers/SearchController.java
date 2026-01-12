package com.grptwo.schedulerapp.controllers;

import com.grptwo.schedulerapp.models.Events;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

public class SearchController {

    @FXML private TextField searchField;
    @FXML private DatePicker datePicker;
    @FXML private VBox resultsContainer;

    private Map<LocalDate, List<Events>> eventsMap;
    private Consumer<Events> onEdit;

    @FXML
    public void initialize() {
        searchField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            HBox wrapper = (HBox) searchField.getParent();
            if (newVal) {
                wrapper.getStyleClass().add("focused");
            } else {
                wrapper.getStyleClass().remove("focused");
            }
        });
    }

    public void init(Map<LocalDate, List<Events>> eventsMap, Consumer<Events> onEdit) {
        this.eventsMap = eventsMap;
        this.onEdit = onEdit;
    }

    @FXML
    public void onSearch() {

        if (eventsMap == null || eventsMap.isEmpty()) {
            showMessage("No events available");
            return;
        }

        String searchText = searchField.getText().trim().toLowerCase();
        LocalDate selectedDate = datePicker.getValue();

        if (searchText.isEmpty() && selectedDate == null) {
            showMessage("Enter a keyword or select a date to search");
            return;
        }

        List<Events> results = new ArrayList<>();

        for (Map.Entry<LocalDate, List<Events>> entry : eventsMap.entrySet()) {
            LocalDate eventDate = entry.getKey();

            if (selectedDate != null && !eventDate.equals(selectedDate)) continue;

            for (Events event : entry.getValue()) {

                if (searchText.isEmpty()) {
                    results.add(event);
                    continue;
                }

                boolean matchTitle = event.getTitle().toLowerCase().contains(searchText);
                boolean matchDesc = event.getDesc() != null &&
                        event.getDesc().toLowerCase().contains(searchText);

                if (matchTitle || matchDesc) {
                    results.add(event);
                }
            }
        }

        if (results.isEmpty()) {
            showMessage("No events found");
        } else {
            showResults(results);
        }
    }

    private void showResults(List<Events> events) {
        resultsContainer.getChildren().clear();

        Label count = new Label(events.size() + " event(s) found");
        count.getStyleClass().add("results-count");
        resultsContainer.getChildren().add(count);

        for (Events event : events) {
            resultsContainer.getChildren().add(makeEventCard(event));
        }
    }

    private VBox makeEventCard(Events event) {
        VBox card = new VBox(8);
        card.getStyleClass().add("result-card");

        LocalDate eventDate = event.getStartDateTime().toLocalDate();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

        Label dateLabel = new Label(eventDate.format(dateFormat));
        dateLabel.getStyleClass().add("result-date");

        HBox timeBox = new HBox(8);
        timeBox.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(4);
        dot.getStyleClass().add("result-dot");

        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
        String start = event.getStartDateTime().format(timeFormat);
        String end = event.getEndDateTime().format(timeFormat);

        Label timeLabel = new Label(start + " - " + end);
        timeLabel.getStyleClass().add("result-time");

        timeBox.getChildren().addAll(dot, timeLabel);

        Label titleLabel = new Label(event.getTitle());
        titleLabel.getStyleClass().add("result-title");
        titleLabel.setWrapText(true);

        VBox content = new VBox(5, timeBox, titleLabel);

        if (event.getDesc() != null && !event.getDesc().isEmpty()) {
            Label descLabel = new Label(event.getDesc());
            descLabel.getStyleClass().add("result-desc");
            descLabel.setWrapText(true);
            content.getChildren().add(descLabel);
        }

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("result-edit-btn");
        editBtn.setOnAction(e -> {
            if (onEdit != null) {
                onEdit.accept(event);
            }
        });

        card.getChildren().addAll(dateLabel, content, editBtn);
        return card;
    }

    private void showMessage(String message) {
        resultsContainer.getChildren().clear();

        Label msgLabel = new Label(message);
        msgLabel.getStyleClass().add("no-results");
        resultsContainer.getChildren().add(msgLabel);
    }
}