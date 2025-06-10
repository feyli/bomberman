module com.bomberman {
    // Modules JavaFX requis
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    // Modules Java standard
    requires java.desktop;
    requires java.prefs;

    // Ouvrir les packages aux modules JavaFX pour la réflexion FXML
    opens com.bomberman to javafx.fxml;
    opens com.bomberman.controller to javafx.fxml;
    opens com.bomberman.model to javafx.base, javafx.fxml;
    opens com.bomberman.view to javafx.graphics, javafx.fxml;
    opens com.bomberman.utils to javafx.fxml;

    // Exporter les packages principaux
    exports com.bomberman;
    exports com.bomberman.controller;
    exports com.bomberman.model;
    exports com.bomberman.view;
    exports com.bomberman.utils;
}