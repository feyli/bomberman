module fr.amu.iut.bomberman {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens fr.amu.iut.bomberman to javafx.fxml;
    exports fr.amu.iut.bomberman;
}