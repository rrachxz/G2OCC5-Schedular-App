package com.grptwo.schedulerapp.controllers;

import javafx.fxml.FXML;

import java.awt.*;
import java.net.URI;

public class ProfileController {
    @FXML
    public void openGithub1() {
        openUrl("https://github.com/rrachxz");
    }

    @FXML
    public void openGithub2() {
        openUrl("https://github.com/keishaqila");
    }

    @FXML
    public void openGithub3() {
        openUrl("https://github.com/24078302Liz");
    }

    @FXML
    public void openGithub4() {
        openUrl("https://github.com/24074901-glitch");
    }

    @FXML
    public void openGithub5() {
        openUrl("https://github.com/Versa03");
    }

    private void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

