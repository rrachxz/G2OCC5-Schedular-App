package com.grptwo.schedulerapp.controllers;

import com.grptwo.schedulerapp.models.Events;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.time.LocalDate;

public class NavbarController {

    public Button addBtn;
    public Button calendarBtn;
    public Button bellBtn;
    public Button profileBtn;

    private Node homepageContent;
    private HomepageController homepageController;

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

    public void setHomepageController(HomepageController controller) {
        this.homepageController = controller;
    }

    @FXML
    public void onCalendarClick() {
        setActiveNavButton(calendarBtn);

        if (homepageContent != null) {
            BorderPane mainContainer = (BorderPane) calendarBtn.getScene().getRoot();
            if (mainContainer != null) {
                mainContainer.setCenter(homepageContent);
            }
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

    @FXML
    public void onAddClick() {
        setActiveNavButton(addBtn);
        loadAddView(null, null);
    }

    public void loadAddView(LocalDate selectedDate, Events editEvent) {
        try {
            BorderPane mainContainer = (BorderPane) calendarBtn.getScene().getRoot();
            if (mainContainer == null) {
                System.err.println("Main container is null!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/grptwo/schedulerapp/views/add.fxml"));
            Node newContent = loader.load();

            AddController controller = loader.getController();
            if (controller == null) {
                System.err.println("AddController is null!");
                return;
            }

            controller.setHomepageController(homepageController);

            if (editEvent != null) {
                controller.setEditMode(editEvent);
            } else if (selectedDate != null) {
                controller.setInitialDate(selectedDate);
            }

            mainContainer.setCenter(newContent);

        } catch (IOException e) {
            System.err.println("Error loading add view:");
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            BorderPane mainContainer = (BorderPane) calendarBtn.getScene().getRoot();
            if (mainContainer == null) {
                System.err.println("Main container is null!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node newContent = loader.load();

            mainContainer.setCenter(newContent);

        } catch (IOException e) {
            System.err.println("Error loading view: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void setActiveNavButton(Button activeButton) {
        calendarBtn.getStyleClass().remove("nav-active");
        bellBtn.getStyleClass().remove("nav-active");
        profileBtn.getStyleClass().remove("nav-active");
        addBtn.getStyleClass().remove("nav-active");

        if (activeButton != null && !activeButton.getStyleClass().contains("nav-active")) {
            activeButton.getStyleClass().add("nav-active");
        }
    }
}