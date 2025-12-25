module com.grptwo.schedulerapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires javafx.graphics;
    requires java.desktop;

    opens com.grptwo.schedulerapp to javafx.fxml;
    exports com.grptwo.schedulerapp;
    exports com.grptwo.schedulerapp.controllers;
    opens com.grptwo.schedulerapp.controllers to javafx.fxml;
}