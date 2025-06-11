package fr.amu.iut.bomberman;

import fr.amu.iut.bomberman.controller.SettingsController;
import fr.amu.iut.bomberman.utils.ProfileManager;
import fr.amu.iut.bomberman.utils.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.util.prefs.Preferences;

/**
 * Classe principale de l'application Super Bomberman Clone
 * Point d'entrée du jeu avec JavaFX
 *
 * @author Groupe_3_6
 * @version 1.0
 */
public class Main extends Application {

    private static final String APP_TITLE = "Super Bomberman Clone";
    private static final int WINDOW_WIDTH = 1024;
    private static final int WINDOW_HEIGHT = 768;

    /**
     * Méthode principale de lancement de l'application JavaFX
     *
     * @param primaryStage Stage principal de l'application
     * @throws Exception En cas d'erreur lors du chargement
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialisation des gestionnaires
        ProfileManager.getInstance().loadProfiles();

        // Charger le thème depuis les préférences utilisateur (obscur par défaut)
        Preferences preferences = Preferences.userNodeForPackage(SettingsController.class);
        String savedTheme = preferences.get("theme", "obscur");
        System.out.println("Chargement du thème depuis les préférences: " + savedTheme);
        ThemeManager.getInstance().loadTheme(savedTheme);

        // Chargement de la vue principale
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainMenu.fxml"));
        Parent root = loader.load();

        // Configuration de la scène
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(getClass().getResource(ThemeManager.getInstance().getThemeCssPath()).toExternalForm());

        // Configuration du stage
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);

        // Ne pas maximiser la fenêtre au démarrage
        // primaryStage.setMaximized(true);

        // Ne pas activer le plein écran au démarrage
        // primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("Appuyez sur F11 pour quitter le mode plein écran");

        // Icône de l'application
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        } catch (Exception e) {
            System.err.println("Impossible de charger l'icône : " + e.getMessage());
        }

        primaryStage.show();
    }

    /**
     * Méthode appelée lors de la fermeture de l'application
     * Sauvegarde les données avant la fermeture
     */
    @Override
    public void stop() {
        ProfileManager.getInstance().saveProfiles();
        System.out.println("Application fermée - Données sauvegardées");
    }

    /**
     * Point d'entrée principal de l'application
     *
     * @param args Arguments de ligne de commande
     */
    public static void main(String[] args) {
        launch(args);
    }
}