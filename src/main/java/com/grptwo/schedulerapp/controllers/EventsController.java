package com.grptwo.schedulerapp.controllers;

import com.grptwo.schedulerapp.models.Events;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.util.ArrayList;
import java.util.List;

public class EventsController {

    private List<Events> events = new ArrayList<>();

    @FXML
    private ListView<String> eventsList;

    @FXML
    private void initialize() {
        eventsList.getItems().addAll();
    }

    @FXML
    private void onAddButtonClick() {
        String data = eventsList.getSelectionModel().getSelectedItem();
    }
}
