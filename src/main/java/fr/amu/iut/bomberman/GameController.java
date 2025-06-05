package fr.amu.iut.bomberman;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

public class GameController {
    private final Game game;
    private GameView view;
    private final InputHandler inputHandler;
    private Timeline gameLoop;

    public GameController() {
        this.game = new Game();
        this.inputHandler = new InputHandler(this);

    }

    public void initialize() {
        setupGameLoop();


        // Initialiser la vue si elle existe
        if (view != null) {
            view.updateBoard(game.getBoard());
            view.updatePlayerInfo(game.getPlayers());
        }
    }

    public void setView(GameView view) {
        this.view = view;
    }

    private void setupGameLoop() {
        gameLoop = new Timeline(new KeyFrame(
                Duration.millis(Constants.GAME_LOOP_DELAY),
                _ -> updateGame()
        ));
        gameLoop.setCycleCount(Animation.INDEFINITE);
    }

    private void updateGame() {
        if (game.isRunning() && game.getCurrentState() == GameState.PLAYING) {
            game.update();

            if (view != null) {
                view.updateBoard(game.getBoard());
                view.updatePlayerInfo(game.getPlayers());
                view.updateGameTimer(game.getGameTimeString());
            }

            // Vérifier si le jeu est terminé
            if (game.getCurrentState() == GameState.GAME_OVER) {
                stopGameLoop();
                if (view != null) {
                    view.showGameOver(game.getWinner());
                }
            }
        }
    }

    public void startNewGame() {
        startNewGame(2); // Par défaut 2 joueurs
    }

    public void startNewGame(int playerCount) {
        game.initializePlayers(playerCount);
        game.startGame();

        if (view != null) {
            view.updateBoard(game.getBoard());
            view.updatePlayerInfo(game.getPlayers());
        }

        startGameLoop();

    }

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

    public void returnToMenu() {
        stopGameLoop();
        game.returnToMenu();

        if (view != null) {
            view.showMainMenu();
        }
    }

    private void startGameLoop() {
        if (gameLoop != null && gameLoop.getStatus() != Animation.Status.RUNNING) {
            gameLoop.play();
        }
    }

    private void stopGameLoop() {
        if (gameLoop != null && gameLoop.getStatus() == Animation.Status.RUNNING) {
            gameLoop.stop();
        }
    }

    public void handlePlayerInput(KeyEvent event) {
        inputHandler.handleKeyPressed(event);
    }

    // Méthodes appelées par InputHandler
    public void movePlayer(int playerId, Direction direction) {
        if (game.getCurrentState() == GameState.PLAYING) {
            game.movePlayer(playerId, direction);

        }
    }

    public void playerPlaceBomb(int playerId) {
        if (game.getCurrentState() == GameState.PLAYING) {
            Player player = game.getPlayerById(playerId);
            if (player != null && player.isAlive()) {
                player.placeBomb(game.getBoard());
            }
        }
    }

    // Getters
    public Game getGame() {
        return game;
    }
}
