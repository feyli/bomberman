package org.example.bomberman;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class GameController {
    private Game game;
    private GameView view;
    private InputHandler inputHandler;
    private Timeline gameLoop;
    private List<Bot> bots; // Liste des bots
    private List<Integer> humanPlayers; // Liste des joueurs humains
    private double botActionTimer = 0; // Timer pour les actions automatiques des bots

    public GameController() {
        this.game = new Game();
        this.inputHandler = new InputHandler(this);
        this.bots = new ArrayList<>(); // Initialisation des bots
        this.humanPlayers = new ArrayList<>(); // Initialisation des joueurs humains
    }

    // Méthode initiale d'initialisation
    public void initialize() {
        setupGameLoop();
        if (view != null) {
            view.updateBoard(game.getBoard());
            view.updatePlayerInfo(game.getPlayers());
        }
    }

    // Définit la vue
    public void setView(GameView view) {
        this.view = view;
    }

    // Configure la boucle de jeu
    private void setupGameLoop() {
        gameLoop = new Timeline(new KeyFrame(
                Duration.millis(Constants.GAME_LOOP_DELAY),
                e -> updateGame()
        ));
        gameLoop.setCycleCount(Animation.INDEFINITE);
    }

    // Mise à jour du jeu (appelée dans la boucle de jeu)
    private void updateGame() {
        if (game.isRunning() && game.getCurrentState() == GameState.PLAYING) {
            game.update();

            if (view != null) {
                view.updateBoard(game.getBoard());
                view.updatePlayerInfo(game.getPlayers());
                view.updateGameTimer(game.getGameTimeString());
            }

            // Actions des bots toutes les 1000 ms
            botActionTimer += Constants.GAME_LOOP_DELAY;
            if (botActionTimer >= 1000) {
                for (Bot bot : bots) {
                    bot.performAction(); // Le bot exécute son action
                }
                botActionTimer = 0; // Réinitialiser le timer
            }

            // Gestion de la fin de partie
            if (game.getCurrentState() == GameState.GAME_OVER) {
                stopGameLoop();
                if (view != null) {
                    view.showGameOver(game.getWinner());
                }
            }
        }
    }

    // Démarrage d'une nouvelle partie avec deux joueurs humains et aucun bot par défaut
    public void startNewGame() {
        startNewGame(2, 0);
    }

    // Surcharge pour choisir uniquement le nombre de joueurs humains
    public void startNewGame(int numHumans) {
        startNewGame(numHumans, 0);
    }

    // Nouvelle partie avec un nombre défini de joueurs humains et de bots
    public void startNewGame(int numHumans, int numBots) {
        // Réinitialisation des listes
        humanPlayers.clear();
        bots.clear();

        // Initialisation des joueurs dans le modèle de jeu
        game.initializePlayers(numHumans + numBots);
        game.startGame();

        // Ajout des joueurs humains
        for (int i = 1; i <= numHumans; i++) {
            humanPlayers.add(i);
        }

        // Création des bots
        for (int i = 0; i < numBots; i++) { // i commence à 0 pour correspondre au nombre de bots
            Position initialPosition = new Position(i, 0); // Exemple : une position initiale unique
            bots.add(new Bot(initialPosition)); // Crée un bot avec une position initiale
        }
        // Mise à jour de la vue
        if (view != null) {
            view.updateBoard(game.getBoard());
            view.updatePlayerInfo(game.getPlayers());
        }

        // Lancement de la boucle de jeu
        startGameLoop();
    }

    // Pause du jeu
    public void pauseGame() {
        game.pauseGame();
        if (game.getCurrentState() == GameState.PAUSED) {
            stopGameLoop();
            if (view != null) {
                view.showPauseMenu();
            }
        } else if (game.getCurrentState() == GameState.PLAYING) {
            startGameLoop();
            if (view != null) {
                view.hidePauseMenu();
            }
        }
    }

    // Fin de partie
    public void endGame() {
        game.endGame();
        stopGameLoop();
        if (view != null) {
            view.showGameOver(game.getWinner());
        }
    }

    // Retour au menu principal
    public void returnToMenu() {
        stopGameLoop();
        game.returnToMenu();
        if (view != null) {
            view.showMainMenu();
        }
    }

    // Démarre la boucle de jeu
    private void startGameLoop() {
        if (gameLoop != null && gameLoop.getStatus() != Animation.Status.RUNNING) {
            gameLoop.play();
        }
    }

    // Arrête la boucle de jeu
    private void stopGameLoop() {
        if (gameLoop != null && gameLoop.getStatus() == Animation.Status.RUNNING) {
            gameLoop.stop();
        }
    }

    // Gère l'entrée clavier des joueurs
    public void handlePlayerInput(KeyEvent event) {
        inputHandler.handleKeyPressed(event);
    }

    // Déplace un joueur ou un bot
    public void movePlayer(int playerId, Direction direction) {
        if (game.getCurrentState() == GameState.PLAYING) {
            game.movePlayer(playerId, direction);
        }
    }

    // Un joueur (ou bot) place une bombe
    public void playerPlaceBomb(int playerId) {
        if (game.getCurrentState() == GameState.PLAYING) {
            Player player = game.getPlayerById(playerId);
            if (player != null && player.isAlive()) {
                player.placeBomb(game.getBoard());
            }
        }
    }

    // Getters pour le modèle et la vue
    public Game getGame() {
        return game;
    }

    public GameView getView() {
        return view;
    }
}