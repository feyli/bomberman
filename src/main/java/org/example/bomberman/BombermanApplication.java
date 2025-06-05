package org.example.bomberman;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class BombermanApplication extends Application {

    private GameController gameController;
    private GameView gameView;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialiser le contrôleur
            gameController = new GameController();

            // Charger le fichier FXML UNE SEULE FOIS
            FXMLLoader loader = new FXMLLoader(getClass().getResource("game.fxml"));
            StackPane root = loader.load(); // Utiliser loader.load() au lieu de FXMLLoader.load()

            // Récupérer la vue depuis le contrôleur FXML
            gameView = loader.getController(); // Maintenant ça fonctionne !

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
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

            // Gérer les événements clavier
            scene.setOnKeyPressed(gameController::handlePlayerInput);

            // Configurer la fenêtre
            primaryStage.setTitle("Bomberman");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();

            // Gérer la fermeture de l'application
            primaryStage.setOnCloseRequest(e -> {
                System.exit(0);
            });

            primaryStage.show();

            // Demander le focus pour les événements clavier
            root.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du démarrage de l'application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}