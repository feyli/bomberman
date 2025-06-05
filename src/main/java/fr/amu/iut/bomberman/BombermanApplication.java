package fr.amu.iut.bomberman;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BombermanApplication extends Application {
    // Create a logger for this class
    private static final Logger LOGGER = Logger.getLogger(BombermanApplication.class.getName());

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialiser le contrôleur
            GameController gameController = new GameController();

            // Charger le fichier FXML UNE SEULE FOIS
            FXMLLoader loader = new FXMLLoader(getClass().getResource("game.fxml"));
            StackPane root = loader.load(); // Utiliser loader.load() au lieu de FXMLLoader.load()

            // Récupérer la vue depuis le contrôleur FXML
            GameView gameView = loader.getController(); // Maintenant ça fonctionne !

            // Vérifier que gameView n'est pas null
            if (gameView == null) {
                System.err.println("Erreur : Impossible de récupérer le contrôleur depuis le FXML");
                return;
            }

            // Lier le contrôleur et la vue
            gameController.setView(gameView);
            gameView.setController(gameController);

            // Initialiser les composants
            gameController.initialize();

            // Configurer la scène
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());

            // Gérer les événements clavier
            scene.setOnKeyPressed(gameController::handlePlayerInput);

            // Configurer la fenêtre
            primaryStage.setTitle("Bomberman");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();

            // Gérer la fermeture de l'application
            primaryStage.setOnCloseRequest(_ -> System.exit(0));

            primaryStage.show();

            // Demander le focus pour les événements clavier
            root.requestFocus();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du démarrage de l'application", e);
            System.err.println("Erreur lors du démarrage de l'application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}