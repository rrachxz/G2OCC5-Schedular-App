package com.grptwo.schedulerapp.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class NavbarController {

    public Button addBtn;
    public Button calendarBtn;
    public Button bellBtn;
    public Button profileBtn;

    private Node homepageContent;

    @FXML
    public void initialize() {
        setActiveNavButton(calendarBtn);

        calendarBtn.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                javafx.application.Platform.runLater(() -> {
                    BorderPane mainContainer = (BorderPane) calendarBtn.getScene().getRoot();
                    if (mainContainer != null && mainContainer.getCenter() != null) {
                        homepageContent = mainContainer.getCenter();
                    }
                });
            }
        });
    }

    @FXML
    public void onCalendarClick() {
        setActiveNavButton(calendarBtn);

        if (homepageContent != null) {
            BorderPane mainContainer = (BorderPane) calendarBtn.getScene().getRoot();
            mainContainer.setCenter(homepageContent);
        }
    }

    @FXML
    public void onBellClick() {
        setActiveNavButton(bellBtn);
        loadView("/com/grptwo/schedulerapp/views/notifs.fxml");
    }

    @FXML
    public void onProfileClick() {
        setActiveNavButton(profileBtn);
        loadView("/com/grptwo/schedulerapp/views/profiles.fxml");
    }

    public void onAddClick() {
        setActiveNavButton(addBtn);
        loadView("/com/grptwo/schedulerapp/views/add.fxml");
    }


    private void loadView(String fxmlPath) {
        try {
            BorderPane mainContainer = (BorderPane) calendarBtn.getScene().getRoot();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node newContent = loader.load();

            mainContainer.setCenter(newContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveNavButton(Button activeButton) {
        calendarBtn.getStyleClass().remove("nav-active");
        bellBtn.getStyleClass().remove("nav-active");
        profileBtn.getStyleClass().remove("nav-active");

        if (!activeButton.getStyleClass().contains("nav-active")) {
            activeButton.getStyleClass().add("nav-active");
        }
    }
}
