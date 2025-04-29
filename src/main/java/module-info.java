module project.automatedtextprocessing {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens project.automatedtextprocessing to javafx.fxml;
    exports project.automatedtextprocessing.controllers;
//    exports project.automatedtextprocessing.controllers;
    opens project.automatedtextprocessing.controllers to javafx.fxml;
}