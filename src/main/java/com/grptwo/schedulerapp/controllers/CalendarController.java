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
import java.time.format.DateTimeFormatter;
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
    private LocalDate selectedDate;
    private Map<LocalDate, List<Events>> eventsMap;
    private Runnable onDateChange;
    private Runnable onAddEventRequest;

    public void init(Map<LocalDate, List<Events>> eventsMap, Runnable onDateChange) {
        this.eventsMap = eventsMap;
        this.onDateChange = onDateChange;
        this.currentMonth = YearMonth.now();
        this.selectedDate = LocalDate.now();
        setupDayLabels();
        updateCal();
    }

    public void setOnAddEventRequest(Runnable onAddEventRequest) {
        this.onAddEventRequest = onAddEventRequest;
    }

    private void setupDayLabels() {
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        dayGrid.getChildren().clear();

        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.getStyleClass().add("day-label");
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setAlignment(Pos.CENTER);
            GridPane.setHgrow(dayLabel, Priority.ALWAYS);
            dayGrid.add(dayLabel, i, 0);
        }
    }

    public void updateCal() {
        calGrid.getChildren().clear();

        monthLabel.setText(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        yearLabel.setText(String.valueOf(currentMonth.getYear()));

        LocalDate firstDay = currentMonth.atDay(1);
        int startDayOfWeek = firstDay.getDayOfWeek().getValue();
        int daysInMonth = currentMonth.lengthOfMonth();

        YearMonth prevMonth = currentMonth.minusMonths(1);
        int prevMonthDays = prevMonth.lengthOfMonth();
        int prevMonthStart = prevMonthDays - startDayOfWeek + 2;

        int row = 0;
        int col = 0;

        for (int day = prevMonthStart; day <= prevMonthDays; day++) {
            addDateCell(prevMonth.atDay(day), row, col, true);
            col++;
        }

        for (int day = 1; day <= daysInMonth; day++) {
            if (col == 7) {
                col = 0;
                row++;
            }
            addDateCell(currentMonth.atDay(day), row, col, false);
            col++;
        }

        int nextMonthDay = 1;
        while (row < 5 || col < 7) {
            if (col == 7) {
                col = 0;
                row++;
                if (row >= 6) break;
            }
            addDateCell(currentMonth.plusMonths(1).atDay(nextMonthDay), row, col, true);
            nextMonthDay++;
            col++;
        }
    }

    private void addDateCell(LocalDate date, int row, int col, boolean otherMonth) {
        VBox cell = new VBox(3);
        cell.setAlignment(Pos.CENTER);
        cell.getStyleClass().add("day-cell");

        if (otherMonth) {
            cell.getStyleClass().add("other-month");
        }

        if (date.equals(LocalDate.now())) {
            cell.getStyleClass().add("today");
        }

        if (date.equals(selectedDate) && !date.equals(LocalDate.now())) {
            cell.getStyleClass().add("selected");
        }

        Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
        cell.getChildren().add(dayNumber);

        // add events dots
        if (eventsMap.containsKey(date)) {
            HBox dotsBox = new HBox(2);
            dotsBox.setAlignment(Pos.CENTER);
            dotsBox.getStyleClass().add("event-dots");

            int eventCount = eventsMap.get(date).size();
            int dotsToShow = Math.min(eventCount, 3); // Max 3 dots

            for (int i = 0; i < dotsToShow; i++) {
                Circle dot = new Circle(2);
                dot.getStyleClass().add("event-dot");
                if (i == 1) dot.getStyleClass().add("secondary");
                if (i == 2) dot.getStyleClass().add("tertiary");
                dotsBox.getChildren().add(dot);
            }
            cell.getChildren().add(dotsBox);
        }

        cell.setOnMouseClicked(event -> {
            selectedDate = date;
            updateCal();
            if (onDateChange != null) {
                onDateChange.run();
            }
            showAddDialog(date);
        });

        GridPane.setHgrow(cell, Priority.ALWAYS);
        GridPane.setVgrow(cell, Priority.ALWAYS);
        calGrid.add(cell, col, row);
    }

    private void showAddDialog(LocalDate date) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Add Event");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        String dateText = date.format(formatter);
        alert.setHeaderText("Add event on " + dateText + "?");

        ButtonType addBtn = new ButtonType("Add Event");
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());

        alert.getButtonTypes().setAll(addBtn, cancelBtn);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == addBtn) {
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
        return selectedDate;
    }
}