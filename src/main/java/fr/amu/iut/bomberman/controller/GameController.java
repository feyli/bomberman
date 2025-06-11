package fr.amu.iut.bomberman.controller;

import fr.amu.iut.bomberman.model.*;
import fr.amu.iut.bomberman.utils.Direction;
import fr.amu.iut.bomberman.utils.FullScreenManager;
import fr.amu.iut.bomberman.utils.SoundManager;
import fr.amu.iut.bomberman.utils.ProfileManager;
import fr.amu.iut.bomberman.utils.ThemeManager;
import fr.amu.iut.bomberman.view.GameRenderer;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Contrôleur principal du jeu
 * Gère la boucle de jeu, les entrées et la logique
 *
 * @author Super Bomberman Team
 * @version 1.0
 */
public class GameController implements GameModel.GameModelListener {

    @FXML
    private Canvas gameCanvas;
    @FXML
    private Label timerLabel;
    @FXML
    private Label roundLabel;
    @FXML
    private Label player1Name;
    @FXML
    private Label player1Score;
    @FXML
    private Label player1Lives;
    @FXML
    private Label player2Name;
    @FXML
    private Label player2Score;
    @FXML
    private Label player2Lives;
    @FXML
    private Label messageLabel;
    @FXML
    private Label gameOverLabel;
    @FXML
    private ImageView player1Avatar;  // Référence à l'ImageView du joueur 1
    @FXML
    private ImageView player2Avatar;  // Référence à l'ImageView du joueur 2
    @FXML
    private Pane gamePane;

    private GameModel gameModel;
    private GameRenderer gameRenderer;
    private AnimationTimer gameLoop;

    // Gestion des touches
    private final Map<KeyCode, Boolean> keysPressed = new HashMap<>();
    private long lastFrameTime = 0;
    private int frameCount = 0;

    // Configuration des touches
    private static final Map<KeyCode, Direction> PLAYER1_KEYS = Map.of(
            KeyCode.Z, Direction.UP,
            KeyCode.S, Direction.DOWN,
            KeyCode.Q, Direction.LEFT,
            KeyCode.D, Direction.RIGHT
    );

    private static final Map<KeyCode, Direction> PLAYER2_KEYS = Map.of(
            KeyCode.UP, Direction.UP,
            KeyCode.DOWN, Direction.DOWN,
            KeyCode.LEFT, Direction.LEFT,
            KeyCode.RIGHT, Direction.RIGHT
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

        // Cacher les messages par défaut
        if (messageLabel != null) {
            messageLabel.setVisible(false);
        }

        if (gameOverLabel != null) {
            gameOverLabel.setVisible(false);
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

        // Définir les avatars personnalisés des joueurs
        String player1AvatarPath = player1Profile.getAvatarPath();
        String player2AvatarPath = player2Profile.getAvatarPath();

        System.out.println("Avatar joueur 1: " + player1AvatarPath);
        System.out.println("Avatar joueur 2: " + player2AvatarPath);

        // Transmettre les chemins d'avatar au renderer ou au modèle de joueur
        if (gameModel.getPlayer1() != null) {
            gameModel.getPlayer1().setAvatarPath(player1AvatarPath);
        }

        if (gameModel.getPlayer2() != null) {
            gameModel.getPlayer2().setAvatarPath(player2AvatarPath);
        }

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
     * Démarre une nouvelle partie avec un bot comme adversaire
     *
     * @param playerProfile Profil du joueur humain
     * @param botDifficulty Niveau de difficulté du bot (Facile, Normal, Difficile)
     * @param roundsToWin   Nombre de rounds à gagner
     * @param timeLimit     Limite de temps par round en secondes
     */
    public void startGameWithBot(PlayerProfile playerProfile, String botDifficulty, int roundsToWin, int timeLimit) {
        System.out.println("Démarrage du jeu contre bot: " + playerProfile.getDisplayName() +
                " contre BOT (difficulté: " + botDifficulty + ") - " +
                roundsToWin + " rounds à gagner, " + timeLimit + " secondes par round");

        // Démarrer le jeu avec les paramètres personnalisés
        gameModel.startNewGame(
                playerProfile.getDisplayName(),
                "BOT", // Nom du bot
                roundsToWin,
                timeLimit
        );

        // Définir l'avatar personnalisé du joueur
        String playerAvatarPath = playerProfile.getAvatarPath();
        System.out.println("Avatar joueur: " + playerAvatarPath);

        if (gameModel.getPlayer1() != null) {
            gameModel.getPlayer1().setAvatarPath(playerAvatarPath);
        }

        // Pour le bot, on peut utiliser un avatar spécifique ou celui par défaut
        if (gameModel.getPlayer2() != null) {
            gameModel.getPlayer2().setAvatarPath("/images/avatars/default.png");
        }

        // Mettre à jour les noms des joueurs dans l'interface
        if (player1Name != null) {
            player1Name.setText(playerProfile.getDisplayName());
        }
        if (player2Name != null) {
            player2Name.setText("BOT"); // Le nom du bot
        }

        // Démarrer la boucle de jeu
        startGameLoop();

        // Créer et activer le bot pour le joueur 2
        initializeBot(botDifficulty);

        // Musique du jeu
        SoundManager.getInstance().playMusic("game_theme");

        // S'assurer que le canvas a le focus
        if (gameCanvas != null) {
            gameCanvas.requestFocus();
        }
    }

    // Bot qui contrôle le joueur 2
    private BotPlayer botPlayer;

    /**
     * Initialise le bot avec la difficulté spécifiée
     *
     * @param difficulty Niveau de difficulté du bot
     */
    private void initializeBot(String difficulty) {
        // Obtenir le joueur 2 du modèle de jeu
        Player botControlledPlayer = gameModel.getPlayer2();
        GameBoard gameBoard = gameModel.getGameBoard();

        if (botControlledPlayer != null && gameBoard != null) {
            // Créer et configurer le bot
            botPlayer = new BotPlayer(botControlledPlayer, gameBoard);

            // Ajuster les paramètres du bot selon la difficulté
            configureBot(difficulty);

            // Activer le bot
            botPlayer.activate();

            // Ajouter un écouteur pour détecter la fin de la partie
            gameModel.addListener(new GameModel.GameModelListener() {
                @Override
                public void onRoundStarted(int roundNumber) {
                    // Activer le bot au début d'un round
                    if (botPlayer != null) {
                        botPlayer.activate();
                    }
                }

                @Override
                public void onRoundEnded(Player winner) {
                    // Désactiver le bot entre les rounds
                    if (botPlayer != null) {
                        botPlayer.deactivate();
                    }
                }

                @Override
                public void onGameEnded(Player winner) {
                    // Désactiver le bot à la fin de la partie
                    if (botPlayer != null) {
                        botPlayer.deactivate();
                    }
                }
            });
        }
    }

    /**
     * Configure les paramètres du bot selon la difficulté
     *
     * @param difficulty Niveau de difficulté
     */
    private void configureBot(String difficulty) {
        // Cette méthode pourrait être utilisée pour ajuster les comportements du bot
        // selon la difficulté, par exemple en modifiant la fréquence des actions
        // ou la stratégie de placement des bombes
        System.out.println("Bot configuré avec difficulté: " + difficulty);
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
        Direction p1Direction = Direction.NONE;
        for (Map.Entry<KeyCode, Direction> entry : PLAYER1_KEYS.entrySet()) {
            if (keysPressed.getOrDefault(entry.getKey(), false)) {
                p1Direction = entry.getValue();
                break;
            }
        }
        if (p1Direction != Direction.NONE) {
            gameModel.movePlayer(1, p1Direction, deltaTime);
            // Debug réduit - seulement tous les 30 frames
            if (frameCount % 30 == 0) {
                System.out.println("Joueur 1 - Direction: " + p1Direction +
                        ", Position: (" + gameModel.getPlayer1().getX() +
                        ", " + gameModel.getPlayer1().getY() + ")");
            }
        }

        // Joueur 2
        Direction p2Direction = Direction.NONE;
        for (Map.Entry<KeyCode, Direction> entry : PLAYER2_KEYS.entrySet()) {
            if (keysPressed.getOrDefault(entry.getKey(), false)) {
                p2Direction = entry.getValue();
                break;
            }
        }
        if (p2Direction != Direction.NONE) {
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
                player1Lives.setText("Vies : " + p1.getLives());
                player1Score.setText("Score : " + gameModel.getPlayer1Score());
            }

            if (p2 != null && player2Lives != null && player2Score != null) {
                player2Lives.setText("Vies : " + p2.getLives());
                player2Score.setText("Score : " + gameModel.getPlayer2Score());
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

            // Mettre à jour les avatars des joueurs
            updatePlayerAvatars();
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour de l'UI: " + e.getMessage());
        }
    }

    /**
     * Met à jour les images des avatars des joueurs
     */
    private void updatePlayerAvatars() {
        try {
            Player p1 = gameModel.getPlayer1();
            Player p2 = gameModel.getPlayer2();

            // Joueur 1
            if (p1 != null && player1Avatar != null) {
                String avatarPath = p1.getAvatarPath();
                if (avatarPath != null && !avatarPath.isEmpty()) {
                    player1Avatar.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(avatarPath))));
                } else {
                    // Chemin d'avatar par défaut si aucun avatar personnalisé
                    player1Avatar.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/avatars/default.png"))));
                }
            }

            // Joueur 2
            if (p2 != null && player2Avatar != null) {
                String avatarPath = p2.getAvatarPath();
                if (avatarPath != null && !avatarPath.isEmpty()) {
                    player2Avatar.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(avatarPath))));
                } else {
                    // Chemin d'avatar par défaut si aucun avatar personnalisé
                    player2Avatar.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/avatars/default.png"))));
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour des avatars: " + e.getMessage());
        }
    }

    /**
     * Affiche un message à l'écran
     */
    private void showMessage(String message) {
        if (messageLabel != null) {
            // Cacher tout message existant d'abord
            hideMessage();
            // Puis afficher le nouveau message
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
     * Affiche un message de Game Over à l'écran (label du haut)
     */
    private void showGameOverMessage(String message) {
        if (gameOverLabel != null) {
            gameOverLabel.setText(message);
            gameOverLabel.setVisible(true);
        }
    }

    /**
     * Cache le message de Game Over
     */
    private void hideGameOverMessage() {
        if (gameOverLabel != null) {
            gameOverLabel.setVisible(false);
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
            // Utiliser le thème actuel au lieu de /css/main.css
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(ThemeManager.getInstance().getThemeCssPath())).toExternalForm());

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
        // Si le jeu est terminé (un joueur a atteint le score nécessaire pour gagner),
        // ne pas afficher le message de fin de round pour éviter la superposition
        if (gameModel.getGameState() == GameModel.GameState.GAME_OVER) {
            // Ne rien faire, la fin de partie sera gérée par onGameEnded
            return;
        }

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
        // N'afficher que le nom du gagnant, sans "Game Over" supplémentaire
        showMessage(winner.getName() + " gagne la partie!");

        // Cacher l'autre label pour éviter toute confusion
        hideGameOverMessage();

        // Arrêter toute musique en cours pour éviter la superposition
        SoundManager.getInstance().stopMusic();
        // Jouer le son de fin de partie
        SoundManager.getInstance().playSound("round_end");  // Ce son est en réalité "game_over.wav" d'après le code

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

}
