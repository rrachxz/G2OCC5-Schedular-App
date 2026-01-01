package com.grptwo.schedulerapp.controllers;

import com.grptwo.schedulerapp.models.Events;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class CalendarController {

    public Button prevBtn;
    public Button nextBtn;
    public Label monthLabel;
    public Label yearLabel;
    public GridPane dayGrid;
    public GridPane calGrid;

    private YearMonth currentMonth;
    private LocalDate selected;
    private Map<LocalDate, List<Events>> eventsMap;
    private Runnable onDateChange;
    private Runnable onAddEventRequest;

    public void init(Map<LocalDate, List<Events>> eventsMap, Runnable onDateChange) {
        this.eventsMap = eventsMap;
        this.onDateChange = onDateChange;
        this.currentMonth = YearMonth.now();
        this.selected = LocalDate.now();
        setupDays();
        updateCal();
    }

    public void setOnAddEventRequest(Runnable onAddEventRequest) {
        this.onAddEventRequest = onAddEventRequest;
    }

    private void setupDays() {
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        dayGrid.getChildren().clear();

        for (int i = 0; i < 7; i++) {
            Label day = new Label(days[i]);
            day.getStyleClass().add("day-label");
            day.setMaxWidth(Double.MAX_VALUE);
            day.setAlignment(Pos.CENTER);
            GridPane.setHgrow(day, Priority.ALWAYS);
            dayGrid.add(day, i, 0);
        }
    }

    public void updateCal() {
        calGrid.getChildren().clear();

        monthLabel.setText(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        yearLabel.setText(String.valueOf(currentMonth.getYear()));

        LocalDate first = currentMonth.atDay(1);
        int dayOfWeek = first.getDayOfWeek().getValue();
        int daysInMonth = currentMonth.lengthOfMonth();

        YearMonth prev = currentMonth.minusMonths(1);
        int prevDays = prev.lengthOfMonth();
        int start = prevDays - dayOfWeek + 2;

        int row = 0, col = 0;

        for (int i = start; i <= prevDays; i++) {
            addCell(prev.atDay(i), row, col, true);
            col++;
        }

        for (int d = 1; d <= daysInMonth; d++) {
            if (col == 7) { col = 0; row++; }
            addCell(currentMonth.atDay(d), row, col, false);
            col++;
        }

        int nextDay = 1;
        while (row < 5 || col < 7) {
            if (col == 7) { col = 0; row++; if (row >= 6) break; }
            addCell(currentMonth.plusMonths(1).atDay(nextDay), row, col, true);
            nextDay++;
            col++;
        }
    }

    private void addCell(LocalDate date, int row, int col, boolean other) {
        VBox cell = new VBox(3);
        cell.setAlignment(Pos.CENTER);
        cell.getStyleClass().add("day-cell");

        if (other) cell.getStyleClass().add("other-month");
        if (date.equals(LocalDate.now())) cell.getStyleClass().add("today");
        if (date.equals(selected) && !date.equals(LocalDate.now())) cell.getStyleClass().add("selected");

        Label day = new Label(String.valueOf(date.getDayOfMonth()));
        cell.getChildren().add(day);

        if (eventsMap.containsKey(date)) {
            HBox dots = new HBox(2);
            dots.setAlignment(Pos.CENTER);
            dots.getStyleClass().add("event-dots");

            int count = Math.min(eventsMap.get(date).size(), 3);
            for (int i = 0; i < count; i++) {
                Circle d = new Circle(2);
                d.getStyleClass().add("event-dot");
                if (i == 1) d.getStyleClass().add("secondary");
                if (i == 2) d.getStyleClass().add("tertiary");
                dots.getChildren().add(d);
            }
            cell.getChildren().add(dots);
        }

        cell.setOnMouseClicked(e -> {
            selected = date;
            updateCal();
            if (onDateChange != null) onDateChange.run();
            showAddEventDialog(date);
        });

        GridPane.setHgrow(cell, Priority.ALWAYS);
        GridPane.setVgrow(cell, Priority.ALWAYS);
        calGrid.add(cell, col, row);
    }

    private void showAddEventDialog(LocalDate date) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Add Event");
        alert.setHeaderText("Add event on " + date.format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy")) + "?");
        alert.setContentText("Would you like to create a new event for this date?");

        ButtonType addButton = new ButtonType("Add Event");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());

        alert.getButtonTypes().setAll(addButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == addButton) {
            if (onAddEventRequest != null) {
                onAddEventRequest.run();
            }
        }
    }

    @FXML
    public void onPrevious() {
        currentMonth = currentMonth.minusMonths(1);
        updateCal();
    }

    @FXML
    public void onNext() {
        currentMonth = currentMonth.plusMonths(1);
        updateCal();
    }

    public LocalDate getSelected() {
        return selected;
    }
}