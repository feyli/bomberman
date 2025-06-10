package fr.amu.iut.bomberman.utils;

import fr.amu.iut.bomberman.controller.SettingsController;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

/**
 * Gestionnaire de mode plein écran
 * Cette classe permet de gérer le mode plein écran de manière cohérente dans l'application
 *
 * @author Super Bomberman Team
 * @version 1.0
 */
public class FullScreenManager {

    private static FullScreenManager instance;
    private boolean isGameInFullScreen = false;

    /**
     * Constructeur privé (Singleton)
     */
    private FullScreenManager() {
        // Charger la préférence de plein écran
        isGameInFullScreen = Preferences.userNodeForPackage(SettingsController.class).getBoolean("fullscreen", false);
    }

    /**
     * Obtient l'instance unique du gestionnaire
     *
     * @return Instance du FullScreenManager
     */
    public static synchronized FullScreenManager getInstance() {
        if (instance == null) {
            instance = new FullScreenManager();
        }
        return instance;
    }

    /**
     * Configure une fenêtre pour le mode menu (sans plein écran)
     *
     * @param stage Stage à configurer
     */
    public void configureForMenu(Stage stage) {
        // Les menus ne sont jamais en plein écran
        stage.setFullScreen(false);
        stage.setMaximized(true);
        stage.centerOnScreen();
    }

    /**
     * Configure une fenêtre pour le mode jeu (avec plein écran si activé)
     *
     * @param stage Stage à configurer
     */
    public void configureForGame(Stage stage) {
        // Le jeu est en plein écran uniquement si l'option est activée
        stage.setFullScreen(isGameInFullScreen);
        stage.centerOnScreen();
    }

    /**
     * Met à jour l'état du plein écran pour le jeu
     *
     * @param fullscreen Nouvel état du plein écran
     */
    public void setGameFullScreen(boolean fullscreen) {
        this.isGameInFullScreen = fullscreen;
        // Sauvegarder la préférence
        Preferences.userNodeForPackage(SettingsController.class).putBoolean("fullscreen", fullscreen);
    }

    /**
     * Retourne l'état actuel du plein écran pour le jeu
     *
     * @return true si le jeu est en plein écran
     */
    public boolean isGameInFullScreen() {
        return isGameInFullScreen;
    }

    /**
     * Quitte le mode plein écran et configure pour le mode menu
     *
     * @param stage Stage à configurer
     */
    public void configureForMenuFromGame(Stage stage) {
        // Quitter le mode plein écran et configurer pour le menu
        stage.setFullScreen(false);
        stage.setMaximized(true);
        stage.centerOnScreen();
    }
}
