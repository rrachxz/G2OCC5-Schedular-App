package com.grptwo.schedulerapp.controllers;

import com.grptwo.schedulerapp.models.Events;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ProfileController {

    @FXML private Button backupBtn;
    @FXML private Button restoreBtn;
    @FXML private Label statusLabel;

    private Map<LocalDate, List<Events>> eventsMap;
    private Runnable onRestore;

    public void init(Map<LocalDate, List<Events>> eventsMap, Runnable onRestore) {
        this.eventsMap = eventsMap;
        this.onRestore = onRestore;
    }

    @FXML
    public void openGithub1() { openUrl("https://github.com/rrachxz"); }
    @FXML
    public void openGithub2() { openUrl("https://github.com/keishaqila"); }
    @FXML
    public void openGithub3() { openUrl("https://github.com/24078302Liz"); }
    @FXML
    public void openGithub4() { openUrl("https://github.com/24074901-glitch"); }
    @FXML
    public void openGithub5() { openUrl("https://github.com/Versa03"); }

    @FXML
    public void onBackup() {
        if (eventsMap == null || eventsMap.isEmpty()) {
            showAlert("No events to backup");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Backup");
        fileChooser.setInitialFileName("events_backup.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(backupBtn.getScene().getWindow());

        if (file != null) {
            try {
                export(file);
                statusLabel.setText("Backup completed to " + file.getName());
                showInfo("Backup successful!", "Events saved to:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert("Backup failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void onRestore() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Backup File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showOpenDialog(restoreBtn.getScene().getWindow());

        if (file != null) {
            try {
                int count = importFromCSV(file);
                statusLabel.setText("Restored " + count + " events from " + file.getName());
                showInfo("Restore successful!", "Imported " + count + " events");

                if (onRestore != null) {
                    onRestore.run();
                }
            } catch (IOException e) {
                showAlert("Restore failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void export(File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        // Update header, include reminder
        writer.write("id,title,description,startDateTime,endDateTime,reminder");
        writer.newLine();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
        int id = 1;

        for (Map.Entry<LocalDate, List<Events>> entry : eventsMap.entrySet()) {
            for (Events event : entry.getValue()) {
                String line = String.format("%d,%s,%s,%s,%s,%d",
                        id++,
                        escapeCsv(event.getTitle()),
                        escapeCsv(event.getDesc()),
                        event.getStartDateTime().format(formatter),
                        event.getEndDateTime().format(formatter),
                        event.getReminderMinutes() // Save reminder time
                );
                writer.write(line);
                writer.newLine();
            }
        }

        writer.close();
    }

    private int importFromCSV(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));

        String line;
        // Skip empty lines and headers
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty() || line.startsWith("id,") || line.startsWith(",")) {
                continue;
            }
            break;
        }

        eventsMap.clear();
        int count = 0;

        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd MMM yyyy");
        DateTimeFormatter formatter3 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        do {
            if (line == null || line.trim().isEmpty()) continue;

            line = line.replaceFirst("^,+", "");
            String[] parts = line.split(",");

            // Requires at least 5 fields (id, title, desc, start, end), reminder is the 6th
            if (parts.length >= 5) {
                try {
                    int id = Integer.parseInt(parts[0].trim());
                    String title = parts[1].trim();
                    String desc = parts[2].trim();
                    String startStr = parts[3].trim();
                    String endStr = parts[4].trim();

                    // Try reading the 6th field (reminder), default to 0 if missing
                    int reminderMins = 0;
                    if (parts.length >= 6) {
                        try {
                            reminderMins = Integer.parseInt(parts[5].trim());
                        } catch (Exception e) {
                            reminderMins = 0;
                        }
                    }

                    java.time.LocalDateTime startDt;
                    java.time.LocalDateTime endDt;

                    try {
                        startDt = java.time.LocalDateTime.parse(startStr, formatter1);
                        endDt = java.time.LocalDateTime.parse(endStr, formatter1);
                    } catch (Exception e1) {
                        try {
                            startDt = java.time.LocalDateTime.parse(startStr, formatter3);
                            endDt = java.time.LocalDateTime.parse(endStr, formatter3);
                        } catch (Exception e2) {
                            LocalDate startDate = LocalDate.parse(startStr, formatter2);
                            LocalDate endDate = LocalDate.parse(endStr, formatter2);
                            startDt = startDate.atTime(9, 0);
                            endDt = endDate.atTime(17, 0);
                        }
                    }

                    // Create event, including reminder
                    Events event = new Events(id, title, desc, startDt, endDt, null, reminderMins);

                    LocalDate date = event.getStartDateTime().toLocalDate();
                    if (!eventsMap.containsKey(date)) {
                        eventsMap.put(date, new java.util.ArrayList<>());
                    }
                    eventsMap.get(date).add(event);
                    count++;
                } catch (Exception e) {
                    System.err.println("Error parsing line: " + line);
                    e.printStackTrace();
                }
            }
        } while ((line = reader.readLine()) != null);

        reader.close();
        return count;
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}