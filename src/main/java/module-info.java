module fr.amu.iut.bomberman {
    // Modules JavaFX requis
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    // Modules Java standard
    requires java.desktop;
    requires java.prefs;

    // Ouvrir les packages aux modules JavaFX pour la réflexion FXML
    opens fr.amu.iut.bomberman to javafx.fxml;
    opens fr.amu.iut.bomberman.controller to javafx.fxml;
    opens fr.amu.iut.bomberman.model to javafx.base, javafx.fxml;
    opens fr.amu.iut.bomberman.view to javafx.graphics, javafx.fxml;
    opens fr.amu.iut.bomberman.utils to javafx.fxml;

    // Exporter les packages principaux
    exports fr.amu.iut.bomberman;
    exports fr.amu.iut.bomberman.controller;
    exports fr.amu.iut.bomberman.model;
    exports fr.amu.iut.bomberman.view;
    exports fr.amu.iut.bomberman.utils;
}