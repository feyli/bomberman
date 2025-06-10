package fr.amu.iut.bomberman.utils;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Gestionnaire de scènes pour les transitions entre les écrans
 * Préserve les paramètres comme le mode plein écran
 *
 * @author Super Bomberman Team
 * @version 1.0
 */
public class SceneManager {

    private static SceneManager instance;
    private final SettingsManager settingsManager;

    /**
     * Constructeur privé (Singleton)
     */
    private SceneManager() {
        settingsManager = SettingsManager.getInstance();
    }

    /**
     * Obtient l'instance unique du gestionnaire
     *
     * @return Instance du SceneManager
     */
    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    /**
     * Change la scène en préservant le mode plein écran et autres paramètres
     *
     * @param stage Stage actuel
     * @param root Noeud racine de la nouvelle scène
     * @param css Chemin vers la feuille de style CSS (peut être null)
     */
    public void changeScene(Stage stage, Parent root, String css) {
        // Récupérer l'état actuel du plein écran avant de changer de scène
        boolean wasFullScreen = stage.isFullScreen();

        Scene scene = new Scene(root);

        // Appliquer la feuille de style si fournie
        if (css != null && !css.isEmpty()) {
            scene.getStylesheets().add(css);
        }

        stage.setScene(scene);

        // Restaurer le mode plein écran si nécessaire
        stage.setFullScreen(wasFullScreen);

        // Centrer la fenêtre
        stage.centerOnScreen();
    }
}
