package fr.amu.iut.bomberman;

import fr.amu.iut.bomberman.utils.ProfileManager;
import fr.amu.iut.bomberman.utils.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Classe principale de l'application Super Bomberman Clone
 * Point d'entrée du jeu avec JavaFX
 *
 * @author Super Bomberman Team
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
        ThemeManager.getInstance().loadTheme("classic");

        // Chargement de la vue principale
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainMenu.fxml"));
        Parent root = loader.load();

        // Configuration de la scène
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());

        // Configuration du stage
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

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