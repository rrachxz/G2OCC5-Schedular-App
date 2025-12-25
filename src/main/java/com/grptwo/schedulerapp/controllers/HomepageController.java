package com.grptwo.schedulerapp.controllers;

import com.grptwo.schedulerapp.models.Events;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

public class HomepageController {

    @FXML
    public VBox navbar;
    @FXML public Button prevBtn;
    @FXML public Button nextBtn;
    @FXML private VBox eventsContainer;
    @FXML private Label monthLabel;
    @FXML private Label yearLabel;
    @FXML private GridPane dayGrid;
    @FXML private GridPane calGrid;

    private YearMonth currentMonth;
    private LocalDate selected;
    private final Map<LocalDate, List<Events>> eventsMap = new HashMap<>();

    @FXML
    public void initialize() {
        currentMonth = YearMonth.now();
        selected = LocalDate.now();
        loadEvents();
        setupDays();
        updateCal();
        updateEvents();
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

    private void updateEvents() {
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
        if (style.equals("secondary")) dot.getStyleClass().add("secondary");
        if (style.equals("tertiary")) dot.getStyleClass().add("tertiary");

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
        btn.setOnAction(e -> showOptions(event));

        card.getChildren().addAll(content, btn);
        return card;
    }

    private void showOptions(Events event) {
        ContextMenu menu = new ContextMenu();
        MenuItem edit = new MenuItem("Edit");
        MenuItem delete = new MenuItem("Delete");

        edit.setOnAction(e -> editEvent(event));
        delete.setOnAction(e -> deleteEvent(event));

        menu.getItems().addAll(edit, delete);
        menu.show(prevBtn.getScene().getWindow(), prevBtn.getLayoutX(), prevBtn.getLayoutY());
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
        updateEvents();
        updateCal();
    }

    private String getTime(LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(DateTimeFormatter.ofPattern("HH:mm"));
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

    private void updateCal() {
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
            updateEvents();
        });

        GridPane.setHgrow(cell, Priority.ALWAYS);
        GridPane.setVgrow(cell, Priority.ALWAYS);
        calGrid.add(cell, col, row);
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
}
