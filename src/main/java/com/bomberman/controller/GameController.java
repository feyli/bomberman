package com.bomberman.controller;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import com.bomberman.model.*;
import com.bomberman.view.GameRenderer;
import com.bomberman.utils.SoundManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur principal du jeu
 * Gère la boucle de jeu, les entrées et la logique
 *
 * @author Super Bomberman Team
 * @version 1.0
 */
public class GameController implements GameModel.GameModelListener {

    @FXML private Canvas gameCanvas;
    @FXML private Pane gamePane;
    @FXML private HBox player1Info;
    @FXML private HBox player2Info;
    @FXML private Label player1Name;
    @FXML private Label player1Lives;
    @FXML private Label player1Score;
    @FXML private Label player2Name;
    @FXML private Label player2Lives;
    @FXML private Label player2Score;
    @FXML private Label timerLabel;
    @FXML private Label roundLabel;
    @FXML private Label messageLabel;

    private GameModel gameModel;
    private GameRenderer gameRenderer;
    private AnimationTimer gameLoop;

    // Gestion des touches
    private Map<KeyCode, Boolean> keysPressed = new HashMap<>();
    private long lastFrameTime = 0;
    private int frameCount = 0;

    // Configuration des touches
    private static final Map<KeyCode, Player.Direction> PLAYER1_KEYS = Map.of(
            KeyCode.Z, Player.Direction.UP,
            KeyCode.S, Player.Direction.DOWN,
            KeyCode.Q, Player.Direction.LEFT,
            KeyCode.D, Player.Direction.RIGHT
    );

    private static final Map<KeyCode, Player.Direction> PLAYER2_KEYS = Map.of(
            KeyCode.UP, Player.Direction.UP,
            KeyCode.DOWN, Player.Direction.DOWN,
            KeyCode.LEFT, Player.Direction.LEFT,
            KeyCode.RIGHT, Player.Direction.RIGHT
    );

    private static final KeyCode PLAYER1_BOMB = KeyCode.SPACE;
    private static final KeyCode PLAYER2_BOMB = KeyCode.ENTER;
    private static final KeyCode PAUSE_KEY = KeyCode.P;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        System.out.println("GameController initialisé");

        // Créer le modèle et le renderer
        gameModel = new GameModel();
        gameModel.addListener(this);

        gameRenderer = new GameRenderer(gameCanvas);

        // Configurer le canvas avec dimensions adaptatives
        setupCanvas();

        // Configurer les événements clavier
        setupKeyboardHandlers();

        // Initialiser l'interface
        updateUI();

        // Cacher le message par défaut
        if (messageLabel != null) {
            messageLabel.setVisible(false);
        }
    }

    /**
     * Configure le canvas pour qu'il s'adapte à la fenêtre
     */
    private void setupCanvas() {
        if (gamePane != null && gameCanvas != null) {
            // Le canvas prend toute la place disponible
            gameCanvas.widthProperty().bind(gamePane.widthProperty());
            gameCanvas.heightProperty().bind(gamePane.heightProperty());

            // Définir une taille minimale raisonnable
            gamePane.setMinWidth(720);
            gamePane.setMinHeight(624);

            // Écouter les changements de taille pour redessiner
            gameCanvas.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (gameRenderer != null && gameModel != null) {
                    render();
                }
            });

            gameCanvas.heightProperty().addListener((obs, oldVal, newVal) -> {
                if (gameRenderer != null && gameModel != null) {
                    render();
                }
            });
        }
    }

    /**
     * Démarre une nouvelle partie
     *
     * @param player1Profile Profil du joueur 1
     * @param player2Profile Profil du joueur 2
     */
    public void startGame(PlayerProfile player1Profile, PlayerProfile player2Profile) {
        System.out.println("Démarrage du jeu entre " + player1Profile.getDisplayName() +
                " et " + player2Profile.getDisplayName());

        // Démarrer le jeu
        gameModel.startNewGame(
                player1Profile.getDisplayName(),
                player2Profile.getDisplayName()
        );

        // Mettre à jour les noms des joueurs
        if (player1Name != null) {
            player1Name.setText(player1Profile.getDisplayName());
        }
        if (player2Name != null) {
            player2Name.setText(player2Profile.getDisplayName());
        }

        // Démarrer la boucle de jeu
        startGameLoop();

        // Musique du jeu
        SoundManager.getInstance().playMusic("game_theme");

        // S'assurer que le canvas a le focus
        if (gameCanvas != null) {
            gameCanvas.requestFocus();
        }
    }

    /**
     * Configure les gestionnaires de touches
     */
    private void setupKeyboardHandlers() {
        if (gameCanvas != null) {
            gameCanvas.setFocusTraversable(true);

            // S'assurer que le canvas a le focus
            gameCanvas.requestFocus();

            // Gestion des événements clavier sur la scène entière
            gameCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.setOnKeyPressed(this::handleKeyPressed);
                    newScene.setOnKeyReleased(this::handleKeyReleased);
                }
            });

            // Gestion directe sur le canvas aussi
            gameCanvas.setOnKeyPressed(this::handleKeyPressed);
            gameCanvas.setOnKeyReleased(this::handleKeyReleased);

            // Événement de clic pour reprendre le focus
            gameCanvas.setOnMouseClicked(e -> gameCanvas.requestFocus());

            System.out.println("Gestionnaires de touches configurés");
        }
    }

    /**
     * Gère l'appui sur une touche
     */
    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        keysPressed.put(code, true);

        // Debug moins verbeux
        if (code == PAUSE_KEY || code == PLAYER1_BOMB || code == PLAYER2_BOMB) {
            System.out.println("Touche spéciale pressée: " + code);
        }

        // Pause
        if (code == PAUSE_KEY) {
            gameModel.togglePause();
            if (gameModel.getGameState() == GameModel.GameState.PAUSED) {
                showMessage("PAUSE");
            } else {
                hideMessage();
            }
        }

        // Bombes
        if (code == PLAYER1_BOMB) {
            System.out.println("Joueur 1 tente de placer une bombe");
            gameModel.placeBomb(1);
        } else if (code == PLAYER2_BOMB) {
            System.out.println("Joueur 2 tente de placer une bombe");
            gameModel.placeBomb(2);
        }

        event.consume();
    }

    /**
     * Gère le relâchement d'une touche
     */
    private void handleKeyReleased(KeyEvent event) {
        keysPressed.put(event.getCode(), false);
        event.consume();
    }

    /**
     * Démarre la boucle de jeu
     */
    private void startGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }

        lastFrameTime = System.nanoTime();

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long currentTime) {
                // Calculer le delta time
                double deltaTime = (currentTime - lastFrameTime) / 1_000_000_000.0;
                lastFrameTime = currentTime;

                // Limiter le delta time (pour éviter les problèmes lors des pauses)
                deltaTime = Math.min(deltaTime, 0.05);

                // Mettre à jour le jeu
                update(deltaTime);

                // Dessiner
                render();
            }
        };

        gameLoop.start();
        System.out.println("Boucle de jeu démarrée");
    }

    /**
     * Met à jour la logique du jeu
     */
    private void update(double deltaTime) {
        if (gameModel.getGameState() != GameModel.GameState.PLAYING) {
            return;
        }

        // Gérer les mouvements des joueurs
        handlePlayerMovements(deltaTime);

        // Mettre à jour le modèle
        gameModel.update(deltaTime);

        // Mettre à jour l'interface
        updateUI();
    }

    /**
     * Gère les mouvements des joueurs
     */
    private void handlePlayerMovements(double deltaTime) {
        // Joueur 1
        Player.Direction p1Direction = Player.Direction.NONE;
        for (Map.Entry<KeyCode, Player.Direction> entry : PLAYER1_KEYS.entrySet()) {
            if (keysPressed.getOrDefault(entry.getKey(), false)) {
                p1Direction = entry.getValue();
                break;
            }
        }
        if (p1Direction != Player.Direction.NONE) {
            gameModel.movePlayer(1, p1Direction, deltaTime);
            // Debug réduit - seulement tous les 30 frames
            if (frameCount % 30 == 0) {
                System.out.println("Joueur 1 - Direction: " + p1Direction +
                        ", Position: (" + gameModel.getPlayer1().getX() +
                        ", " + gameModel.getPlayer1().getY() + ")");
            }
        }

        // Joueur 2
        Player.Direction p2Direction = Player.Direction.NONE;
        for (Map.Entry<KeyCode, Player.Direction> entry : PLAYER2_KEYS.entrySet()) {
            if (keysPressed.getOrDefault(entry.getKey(), false)) {
                p2Direction = entry.getValue();
                break;
            }
        }
        if (p2Direction != Player.Direction.NONE) {
            gameModel.movePlayer(2, p2Direction, deltaTime);
            // Debug réduit - seulement tous les 30 frames
            if (frameCount % 30 == 0) {
                System.out.println("Joueur 2 - Direction: " + p2Direction +
                        ", Position: (" + gameModel.getPlayer2().getX() +
                        ", " + gameModel.getPlayer2().getY() + ")");
            }
        }

        frameCount++;
    }

    /**
     * Effectue le rendu du jeu
     */
    private void render() {
        try {
            if (gameRenderer != null && gameModel != null) {
                gameRenderer.render(gameModel);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du rendu: " + e.getMessage());
        }
    }

    /**
     * Met à jour l'interface utilisateur
     */
    private void updateUI() {
        try {
            Player p1 = gameModel.getPlayer1();
            Player p2 = gameModel.getPlayer2();

            if (p1 != null && player1Lives != null && player1Score != null) {
                player1Lives.setText("Vies: " + p1.getLives());
                player1Score.setText("Score: " + gameModel.getPlayer1Score());
            }

            if (p2 != null && player2Lives != null && player2Score != null) {
                player2Lives.setText("Vies: " + p2.getLives());
                player2Score.setText("Score: " + gameModel.getPlayer2Score());
            }

            // Timer
            if (timerLabel != null) {
                int timeRemaining = gameModel.getTimeRemaining();
                int minutes = timeRemaining / 60;
                int seconds = timeRemaining % 60;
                timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
            }

            // Round
            if (roundLabel != null) {
                roundLabel.setText("Round " + gameModel.getCurrentRound());
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour de l'UI: " + e.getMessage());
        }
    }

    /**
     * Affiche un message à l'écran
     */
    private void showMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setVisible(true);
        }
    }

    /**
     * Cache le message
     */
    private void hideMessage() {
        if (messageLabel != null) {
            messageLabel.setVisible(false);
        }
    }

    /**
     * Retour au menu principal
     */
    @FXML
    private void handleBackToMenu() {
        System.out.println("Retour au menu principal");

        // Arrêter la boucle de jeu
        if (gameLoop != null) {
            gameLoop.stop();
        }

        // Arrêter la musique
        SoundManager.getInstance().stopMusic();

        try {
            // Charger le menu principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainMenu.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());

            Stage stage = (Stage) gameCanvas.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du retour au menu: " + e.getMessage());
        }
    }

    // Implémentation de GameModel.GameModelListener

    @Override
    public void onGameStarted() {
        SoundManager.getInstance().playSound("game_start");
        System.out.println("Jeu démarré");
    }

    @Override
    public void onRoundStarted(int roundNumber) {
        showMessage("Round " + roundNumber);
        System.out.println("Round " + roundNumber + " démarré");

        // Cacher le message après 2 secondes
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Platform.runLater(this::hideMessage);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @Override
    public void onRoundEnded(Player winner) {
        if (winner != null) {
            showMessage(winner.getName() + " gagne le round!");
        } else {
            showMessage("Match nul!");
        }
        SoundManager.getInstance().playSound("round_end");

        System.out.println("Round terminé - Gagnant: " + (winner != null ? winner.getName() : "Match nul"));

        // Continuer après 3 secondes
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> {
                    gameModel.continueToNextRound();
                    hideMessage();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @Override
    public void onGameEnded(Player winner) {
        showMessage(winner.getName() + " gagne la partie!");
        SoundManager.getInstance().playSound("victory");

        if (gameLoop != null) {
            gameLoop.stop();
        }

        System.out.println("Partie terminée - Gagnant: " + winner.getName());

        // Retourner au menu après 5 secondes
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Platform.runLater(this::handleBackToMenu);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @Override
    public void onPlayerHit(Player player) {
        SoundManager.getInstance().playSound("player_hit");
        System.out.println("Joueur touché: " + player.getName());
    }

    @Override
    public void onBombPlaced(Player player, Bomb bomb) {
        SoundManager.getInstance().playSound("bomb_place");
        System.out.println("Bombe placée par " + player.getName() + " en (" + bomb.getX() + ", " + bomb.getY() + ")");
    }

    @Override
    public void onPowerUpCollected(Player player, PowerUp powerUp) {
        SoundManager.getInstance().playSound("powerup_collect");

        // Afficher le type de power-up collecté
        String message = player.getName() + ": " + powerUp.getType().getName();
        System.out.println("Power-up collecté: " + message);

        // Afficher temporairement ce message
        showMessage(message);
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                Platform.runLater(this::hideMessage);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Nettoyage lors de la fermeture
     */
    public void cleanup() {
        System.out.println("Nettoyage du GameController");

        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }

        SoundManager.getInstance().stopMusic();
    }
}