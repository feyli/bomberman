package org.example.bomberman;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.InputMismatchException;
import java.util.Scanner;

public class BombermanApplication extends Application {

    private GameController gameController;
    private GameView gameView;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialiser le contrôleur de jeu
            gameController = new GameController();

            // Afficher le menu pour configurer la partie


            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("game.fxml"));
            StackPane root = loader.load();

            // Récupérer la vue à partir du fichier FXML
            gameView = loader.getController();

            // Vérifier si la vue a bien été récupérée
            if (gameView == null) {
                System.err.println("Erreur : Impossible de récupérer la vue depuis le fichier FXML.");
                return;
            }

            // Lier la vue au contrôleur
            gameController.setView(gameView);
            gameView.setController(gameController);

            // Initialiser la vue et le contrôleur
            gameController.initialize();

            // Configurer la scène
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

            // Gérer les événements clavier
            scene.setOnKeyPressed(gameController::handlePlayerInput);

            // Configurer la fenêtre principale
            primaryStage.setTitle("Bomberman");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();

            // Gérer la fermeture correcte de l'application
            primaryStage.setOnCloseRequest(e -> System.exit(0));

            // Afficher la fenêtre graphique
            primaryStage.show();

            // Donner le focus pour les événements clavier
            root.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du démarrage de l'application : " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}