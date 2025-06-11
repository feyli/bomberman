package fr.amu.iut.bomberman.utils;

import javafx.stage.Stage;

import java.util.prefs.Preferences;

/**
 * Gestionnaire de mode plein écran pour l'application
 *
 * @author Groupe_3_6
 * @version 1.0
 */
public class FullScreenManager {
    private static FullScreenManager instance;
    private final Preferences preferences;

    /**
     * Constructeur privé (singleton)
     */
    private FullScreenManager() {
        preferences = Preferences.userNodeForPackage(FullScreenManager.class);
    }

    /**
     * Obtient l'instance unique du gestionnaire
     *
     * @return Instance du gestionnaire
     */
    public static FullScreenManager getInstance() {
        if (instance == null) {
            instance = new FullScreenManager();
        }
        return instance;
    }

    /**
     * Configure une scène pour le menu en venant du jeu
     *
     * @param stage Stage à configurer
     */
    public void configureForMenuFromGame(Stage stage) {
        boolean fullscreen = preferences.getBoolean("fullscreen", false);
        stage.setFullScreen(fullscreen);
    }

    /**
     * Configure une scène pour le menu
     *
     * @param stage Stage à configurer
     */
    public void configureForMenu(Stage stage) {
        boolean fullscreen = preferences.getBoolean("fullscreen", false);
        stage.setFullScreen(fullscreen);
    }

    /**
     * Configure une scène pour le jeu
     *
     * @param stage Stage à configurer
     */
    public void configureForGame(Stage stage) {
        boolean fullscreen = preferences.getBoolean("fullscreen", false);
        stage.setFullScreen(fullscreen);
    }
}
