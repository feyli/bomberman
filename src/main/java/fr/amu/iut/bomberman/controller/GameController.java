package fr.amu.iut.bomberman.controller;

import fr.amu.iut.bomberman.model.*;
import fr.amu.iut.bomberman.utils.FullScreenManager;
import fr.amu.iut.bomberman.utils.ProfileManager;
import fr.amu.iut.bomberman.utils.SoundManager;
import fr.amu.iut.bomberman.utils.ThemeManager;
import fr.amu.iut.bomberman.view.GameRenderer;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * Contrôleur pour le jeu Bomberman
 * Gère la logique du jeu et les interactions utilisateur
 *
 * @author Super Bomberman Team
 * @version 1.0
 */
public class GameController implements GameModel.GameModelListener {

    // Interface FXML
    @FXML
    private BorderPane gameRoot;
    @FXML
    private Canvas gameCanvas;
    @FXML
    private Label timerLabel;
    @FXML
    private Label roundLabel;
    @FXML
    private Label player1NameLabel;
    @FXML
    private Label player1ScoreLabel;
    @FXML
    private Label player1LivesLabel;
    @FXML
    private Label player2NameLabel;
    @FXML
    private Label player2ScoreLabel;
    @FXML
    private Label player2LivesLabel;
    @FXML
    private VBox messageOverlay;
    @FXML
    private Label messageLabel;
    @FXML
    private Button messageButton;
    @FXML
    private HBox controlBar;

    // Gestion des touches
    private final Map<KeyCode, Boolean> keysPressed = new HashMap<>();
    private long lastFrameTime = 0;
    private int frameCount = 0;

    // Configuration dynamique des touches
    private Map<KeyCode, Player.Direction> player1Keys = new HashMap<>();
    private Map<KeyCode, Player.Direction> player2Keys = new HashMap<>();
    private KeyCode player1Bomb;
    private KeyCode player2Bomb;
    private static final KeyCode PAUSE_KEY = KeyCode.P;  // Garde la touche pause fixe

    // Modèle et rendu
    private GameModel gameModel;
    private GameRenderer gameRenderer;
    private SoundManager soundManager;
    private ThemeManager themeManager;
    private AnimationTimer gameLoop;
    private Preferences preferences;

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

        // Charger les préférences
        loadPreferences();
    }

    /**
     * Configure le canvas pour qu'il s'adapte à la fenêtre
     */
    private void setupCanvas() {
        if (gameRoot != null && gameCanvas != null) {
            // Le canvas prend toute la place disponible
            gameCanvas.widthProperty().bind(gameRoot.widthProperty());
            gameCanvas.heightProperty().bind(gameRoot.heightProperty());

            // Définir une taille minimale raisonnable
            gameRoot.setMinWidth(720);
            gameRoot.setMinHeight(624);

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
     * Démarre une nouvelle partie avec paramètres personnalisés
     *
     * @param player1Profile Profil du joueur 1
     * @param player2Profile Profil du joueur 2
     * @param roundsToWin    Nombre de rounds à gagner
     * @param timeLimit      Limite de temps par round en secondes
     */
    public void startGame(PlayerProfile player1Profile, PlayerProfile player2Profile, int roundsToWin, int timeLimit) {
        System.out.println("Démarrage du jeu entre " + player1Profile.getDisplayName() +
                " et " + player2Profile.getDisplayName() +
                " - " + roundsToWin + " rounds à gagner, " + timeLimit + " secondes par round");

        // Démarrer le jeu avec les paramètres personnalisés
        gameModel.startNewGame(
                player1Profile.getDisplayName(),
                player2Profile.getDisplayName(),
                roundsToWin,
                timeLimit
        );

        // Mettre à jour les noms des joueurs
        if (player1NameLabel != null) {
            player1NameLabel.setText(player1Profile.getDisplayName());
        }
        if (player2NameLabel != null) {
            player2NameLabel.setText(player2Profile.getDisplayName());
        }

        // S'assurer que le canvas a une taille valide
        if (gameCanvas != null) {
            if (gameCanvas.getWidth() <= 0 || gameCanvas.getHeight() <= 0) {
                gameCanvas.setWidth(720);
                gameCanvas.setHeight(624);
                System.out.println("Canvas dimensionné explicitement: " + gameCanvas.getWidth() + "x" + gameCanvas.getHeight());
            } else {
                System.out.println("Taille actuelle du canvas: " + gameCanvas.getWidth() + "x" + gameCanvas.getHeight());
            }
        }

        // Effectuer un rendu initial pour éviter l'écran noir
        render();

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
        if (code == PAUSE_KEY || code == player1Bomb || code == player2Bomb) {
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
        if (code == player1Bomb) {
            System.out.println("Joueur 1 tente de placer une bombe");
            gameModel.placeBomb(1);
        } else if (code == player2Bomb) {
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
        for (Map.Entry<KeyCode, Player.Direction> entry : player1Keys.entrySet()) {
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
        for (Map.Entry<KeyCode, Player.Direction> entry : player2Keys.entrySet()) {
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

            if (p1 != null && player1LivesLabel != null && player1ScoreLabel != null) {
                player1LivesLabel.setText("Vies : " + p1.getLives());
                player1ScoreLabel.setText("Score : " + gameModel.getPlayer1Score());
            }

            if (p2 != null && player2LivesLabel != null && player2ScoreLabel != null) {
                player2LivesLabel.setText("Vies : " + p2.getLives());
                player2ScoreLabel.setText("Score : " + gameModel.getPlayer2Score());
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
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/main.css")).toExternalForm());

            Stage stage = (Stage) gameCanvas.getScene().getWindow();
            stage.setScene(scene);

            // Utiliser le FullScreenManager pour configurer le mode menu
            FullScreenManager.getInstance().configureForMenuFromGame(stage);

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

        // Mettre à jour les statistiques du gagnant via ProfileManager
        // Vérification si le profil existe avant la mise à jour
        // Ajout de messages de débogage pour vérifier les étapes
        System.out.println("Débogage: Nom du gagnant: " + winner.getName());

        // Recherche du profil par pseudo (nickname) pour le gagnant
        PlayerProfile winnerProfile = ProfileManager.getInstance().getProfileByNickname(winner.getName());
        if (winnerProfile != null) {
            winnerProfile.updateStats(true, winner.getScore()); // Mise à jour des stats avec victoire
            ProfileManager.getInstance().updateProfile(winnerProfile);
            System.out.println("Statistiques du gagnant " + winner.getName() + " mises à jour.");
        } else {
            System.err.println("Profil introuvable pour le joueur gagnant: " + winner.getName());
        }

        // Identifier le joueur perdant (l'autre joueur)
        Player loser = (winner == gameModel.getPlayer1()) ? gameModel.getPlayer2() : gameModel.getPlayer1();

        // Mettre à jour les statistiques du perdant
        PlayerProfile loserProfile = ProfileManager.getInstance().getProfileByNickname(loser.getName());
        if (loserProfile != null) {
            loserProfile.updateStats(false, loser.getScore()); // Mise à jour des stats sans victoire
            ProfileManager.getInstance().updateProfile(loserProfile);
            System.out.println("Statistiques du perdant " + loser.getName() + " mises à jour.");
        } else {
            System.err.println("Profil introuvable pour le joueur perdant: " + loser.getName());
        }

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
     * Charge les préférences de l'utilisateur pour les touches
     */
    private void loadPreferences() {
        // Charger depuis les préférences de SettingsController
        preferences = Preferences.userNodeForPackage(SettingsController.class);

        // Initialiser les valeurs par défaut
        Map<KeyCode, Player.Direction> defaultP1Keys = new HashMap<>();
        defaultP1Keys.put(KeyCode.Z, Player.Direction.UP);
        defaultP1Keys.put(KeyCode.S, Player.Direction.DOWN);
        defaultP1Keys.put(KeyCode.Q, Player.Direction.LEFT);
        defaultP1Keys.put(KeyCode.D, Player.Direction.RIGHT);

        Map<KeyCode, Player.Direction> defaultP2Keys = new HashMap<>();
        defaultP2Keys.put(KeyCode.UP, Player.Direction.UP);
        defaultP2Keys.put(KeyCode.DOWN, Player.Direction.DOWN);
        defaultP2Keys.put(KeyCode.LEFT, Player.Direction.LEFT);
        defaultP2Keys.put(KeyCode.RIGHT, Player.Direction.RIGHT);

        KeyCode defaultP1Bomb = KeyCode.SPACE;
        KeyCode defaultP2Bomb = KeyCode.ENTER;

        // Charger les contrôles personnalisés
        try {
            // Directions joueur 1
            KeyCode p1Up = KeyCode.valueOf(preferences.get("key.p1.up", "Z"));
            KeyCode p1Down = KeyCode.valueOf(preferences.get("key.p1.down", "S"));
            KeyCode p1Left = KeyCode.valueOf(preferences.get("key.p1.left", "Q"));
            KeyCode p1Right = KeyCode.valueOf(preferences.get("key.p1.right", "D"));
            KeyCode p1BombKey = KeyCode.valueOf(preferences.get("key.p1.bomb", "SPACE"));

            // Directions joueur 2
            KeyCode p2Up = KeyCode.valueOf(preferences.get("key.p2.up", "UP"));
            KeyCode p2Down = KeyCode.valueOf(preferences.get("key.p2.down", "DOWN"));
            KeyCode p2Left = KeyCode.valueOf(preferences.get("key.p2.left", "LEFT"));
            KeyCode p2Right = KeyCode.valueOf(preferences.get("key.p2.right", "RIGHT"));
            KeyCode p2BombKey = KeyCode.valueOf(preferences.get("key.p2.bomb", "ENTER"));

            // Créer les maps de touches
            player1Keys = new HashMap<>();
            player1Keys.put(p1Up, Player.Direction.UP);
            player1Keys.put(p1Down, Player.Direction.DOWN);
            player1Keys.put(p1Left, Player.Direction.LEFT);
            player1Keys.put(p1Right, Player.Direction.RIGHT);

            player2Keys = new HashMap<>();
            player2Keys.put(p2Up, Player.Direction.UP);
            player2Keys.put(p2Down, Player.Direction.DOWN);
            player2Keys.put(p2Left, Player.Direction.LEFT);
            player2Keys.put(p2Right, Player.Direction.RIGHT);

            // Définir les touches de bombe
            player1Bomb = p1BombKey;
            player2Bomb = p2BombKey;

            System.out.println("Touches personnalisées chargées avec succès");
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des touches personnalisées: " + e.getMessage());
            System.out.println("Utilisation des touches par défaut");

            // Utiliser les valeurs par défaut en cas d'erreur
            player1Keys = defaultP1Keys;
            player2Keys = defaultP2Keys;
            player1Bomb = defaultP1Bomb;
            player2Bomb = defaultP2Bomb;
        }

        System.out.println("Contrôles chargés: ");
        System.out.println("Joueur 1 - Mouvements: " + player1Keys.keySet() + ", Bombe: " + player1Bomb);
        System.out.println("Joueur 2 - Mouvements: " + player2Keys.keySet() + ", Bombe: " + player2Bomb);
    }

    /**
     * Analyse une chaîne de caractères pour en faire des bindings de touches
     */
    private Map<KeyCode, Player.Direction> parseKeyBindings(String keyBindings) {
        Map<KeyCode, Player.Direction> keysMap = new HashMap<>();
        String[] bindings = keyBindings.split(";");
        for (String binding : bindings) {
            String[] parts = binding.split(",");
            if (parts.length == 2) {
                try {
                    KeyCode key = KeyCode.valueOf(parts[0]);
                    Player.Direction direction = Player.Direction.valueOf(parts[1]);
                    keysMap.put(key, direction);
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'analyse des bindings de touches: " + e.getMessage());
                }
            }
        }
        return keysMap;
    }

    /**
     * Sauvegarde les préférences de l'utilisateur pour les touches
     */
    private void savePreferences() {
        try {
            preferences = Preferences.userNodeForPackage(getClass());

            // Sauvegarder les touches du joueur 1
            preferences.put("player1Keys", serializeKeyBindings(player1Keys));
            // Sauvegarder les touches du joueur 2
            preferences.put("player2Keys", serializeKeyBindings(player2Keys));
            // Sauvegarder les touches de bombe
            preferences.put("player1Bomb", player1Bomb.name());
            preferences.put("player2Bomb", player2Bomb.name());

            preferences.flush();
            System.out.println("Préférences sauvegardées");
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde des préférences: " + e.getMessage());
        }
    }

    /**
     * Sérialise les bindings de touches en une chaîne de caractères
     */
    private String serializeKeyBindings(Map<KeyCode, Player.Direction> keysMap) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<KeyCode, Player.Direction> entry : keysMap.entrySet()) {
            sb.append(entry.getKey().name()).append(",").append(entry.getValue().name()).append(";");
        }
        return sb.toString();
    }

}
