package org.example.bomberman;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Game {
    private GameState currentState;
    private GameBoard board;
    private List<Player> players;
    private boolean isRunning;
    private int gameTime; // en millisecondes
    private Player winner;

    public Game() {
        this.currentState = GameState.MENU;
        this.board = new GameBoard();
        this.players = new ArrayList<>();
        this.isRunning = false;
        this.gameTime = 0;
        this.winner = null;
    }

    public void initializePlayers(int playerCount) {
        players.clear();
        Point[] spawnPositions = board.getPlayerSpawnPositions();

        String[] playerNames = {"Joueur 1", "Joueur 2", "Joueur 3", "Joueur 4"};

        for (int i = 0; i < Math.min(playerCount, Constants.MAX_PLAYERS); i++) {
            Point spawn = spawnPositions[i];
            Player player = new Player(i + 1, playerNames[i], spawn.x, spawn.y);
            players.add(player);
        }
    }

    public void startGame() {
        if (players.isEmpty()) {
            initializePlayers(2); // Par défaut 2 joueurs
        }

        currentState = GameState.PLAYING;
        isRunning = true;
        gameTime = 0;
        winner = null;

        // Réinitialiser le plateau
        board = new GameBoard();

        // Replacer les joueurs aux positions de spawn
        Point[] spawnPositions = board.getPlayerSpawnPositions();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            Point spawn = spawnPositions[i];
            // Réinitialiser les propriétés du joueur
            players.set(i, new Player(player.getId(), player.getName(), spawn.x, spawn.y));
        }
    }

    public void pauseGame() {
        if (currentState == GameState.PLAYING) {
            currentState = GameState.PAUSED;
            isRunning = false;
        } else if (currentState == GameState.PAUSED) {
            currentState = GameState.PLAYING;
            isRunning = true;
        }
    }

    public void endGame() {
        currentState = GameState.GAME_OVER;
        isRunning = false;
    }

    public void update() {
        if (!isRunning || currentState != GameState.PLAYING) {
            return;
        }

        gameTime += Constants.GAME_LOOP_DELAY;

        // Mettre à jour le plateau (bombes, explosions)
        board.update();

        // Vérifier les collisions avec les explosions
        checkExplosionCollisions();

        // Vérifier les conditions de victoire
        checkWinConditions();
    }

    private void checkExplosionCollisions() {
        for (Player player : players) {
            if (player.isAlive() && board.isPlayerInExplosion(player)) {
                player.takeDamage();
            }
        }
    }

    private void checkWinConditions() {
        List<Player> alivePlayers = new ArrayList<>();
        for (Player player : players) {
            if (player.isAlive()) {
                alivePlayers.add(player);
            }
        }

        if (alivePlayers.size() <= 1) {
            if (alivePlayers.size() == 1) {
                winner = alivePlayers.get(0);
            } else {
                winner = null; // Égalité
            }
            endGame();
        }
    }

    public void movePlayer(int playerId, org.example.bomberman.Direction direction) {
        if (currentState != GameState.PLAYING) return;

        Player player = getPlayerById(playerId);
        if (player != null && player.isAlive()) {
            player.move(direction, board);
        }
    }

    public void playerPlaceBomb(int playerId) {
        if (currentState != GameState.PLAYING) return;

        Player player = getPlayerById(playerId);
        if (player != null && player.isAlive()) {
            player.placeBomb(board);
        }
    }

    public Player getPlayerById(int id) {
        for (Player player : players) {
            if (player.getId() == id) {
                return player;
            }
        }
        return null;
    }

    public List<Player> getAlivePlayers() {
        List<Player> alive = new ArrayList<>();
        for (Player player : players) {
            if (player.isAlive()) {
                alive.add(player);
            }
        }
        return alive;
    }

    public String getGameTimeString() {
        int seconds = gameTime / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void returnToMenu() {
        currentState = GameState.MENU;
        isRunning = false;
        winner = null;
    }

    // Getters
    public GameState getCurrentState() {
        return currentState;
    }

    public GameBoard getBoard() {
        return board;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getGameTime() {
        return gameTime;
    }

    public Player getWinner() {
        return winner;
    }

    // Setters
    public void setCurrentState(GameState state) {
        this.currentState = state;
    }
}
