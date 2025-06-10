package fr.amu.iut.bomberman.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Modèle principal du jeu Bomberman
 * Gère l'état global du jeu et la logique
 *
 * @author Super Bomberman Team
 * @version 1.0
 */
public class GameModel {

    /**
     * États possibles du jeu
     */
    public enum GameState {
        MENU,
        PLAYING,
        PAUSED,
        ROUND_OVER,
        GAME_OVER
    }

    private final ObjectProperty<GameState> gameState;
    private final IntegerProperty currentRound;
    private final IntegerProperty player1Score;
    private final IntegerProperty player2Score;
    private final IntegerProperty timeRemaining;

    private GameBoard gameBoard;
    private Player player1;
    private Player player2;
    private final int roundsToWin;
    private final int roundTimeLimit; // en secondes
    private int frameCount = 0;

    private List<GameModelListener> listeners;

    /**
     * Constructeur du modèle de jeu
     */
    public GameModel() {
        this.gameState = new SimpleObjectProperty<>(GameState.MENU);
        this.currentRound = new SimpleIntegerProperty(1);
        this.player1Score = new SimpleIntegerProperty(0);
        this.player2Score = new SimpleIntegerProperty(0);
        this.timeRemaining = new SimpleIntegerProperty(180); // 3 minutes par défaut

        this.gameBoard = new GameBoard();
        this.roundsToWin = 3;
        this.roundTimeLimit = 180;
        this.listeners = new ArrayList<>();
    }

    /**
     * Initialise une nouvelle partie
     *
     * @param player1Name Nom du joueur 1
     * @param player2Name Nom du joueur 2
     */
    public void startNewGame(String player1Name, String player2Name) {
        // Créer les joueurs avec positions corrigées
        // Joueur 1 en haut à gauche (1,1)
        player1 = new Player(1, player1Name, 1.5, 1.5);

        // Joueur 2 en bas à droite - CORRECTION ICI
        // La grille fait 15x13, donc le coin bas-droit est (13,11)
        player2 = new Player(2, player2Name, 13.5, 11.5);

        System.out.println("=== NOUVELLE PARTIE ===");
        System.out.println("Grille: " + GameBoard.GRID_WIDTH + "x" + GameBoard.GRID_HEIGHT);
        System.out.println("Joueur 1 créé à la position: (" + player1.getX() + ", " + player1.getY() + ")");
        System.out.println("Joueur 2 créé à la position: (" + player2.getX() + ", " + player2.getY() + ")");

        // Réinitialiser les scores
        player1Score.set(0);
        player2Score.set(0);
        currentRound.set(1);

        // Démarrer le premier round
        startNewRound();
        gameState.set(GameState.PLAYING);

        notifyGameStarted();
    }

    /**
     * Démarre un nouveau round
     */
    private void startNewRound() {
        gameBoard.reset();

        // Réinitialiser les joueurs aux bonnes positions
        player1.reset(1.5, 1.5);
        player2.reset(13.5, 11.5);  // CORRECTION ICI

        System.out.println("=== NOUVEAU ROUND ===");
        System.out.println("Round " + currentRound.get() + " démarré");
        System.out.println("Joueur 1 reset à: (" + player1.getX() + ", " + player1.getY() + ")");
        System.out.println("Joueur 2 reset à: (" + player2.getX() + ", " + player2.getY() + ")");

        timeRemaining.set(roundTimeLimit);

        notifyRoundStarted();
    }

    /**
     * Met à jour l'état du jeu
     *
     * @param deltaTime Temps écoulé depuis la dernière mise à jour
     */
    public void update(double deltaTime) {
        if (gameState.get() != GameState.PLAYING) {
            return;
        }

        // Mise à jour du temps
        updateTimer(deltaTime);

        // Mise à jour du plateau
        List<Bomb> explodedBombs = gameBoard.update(deltaTime);
        for (Bomb bomb : explodedBombs) {
            gameBoard.explodeBomb(bomb);
            // Décrémenter le compteur de bombes du joueur
            if (bomb.getOwnerId() == 1) {
                player1.decrementBombsPlaced();
            } else if (bomb.getOwnerId() == 2) {
                player2.decrementBombsPlaced();
            }
        }

        // Vérifier les collisions avec les explosions
        checkExplosionCollisions();

        // Vérifier la collecte de power-ups
        checkPowerUpCollection();

        // Vérifier la fin du round
        checkRoundEnd();
    }

    /**
     * Met à jour le timer du round
     *
     * @param deltaTime Temps écoulé
     */
    private void updateTimer(double deltaTime) {
        int newTime = timeRemaining.get() - (int) deltaTime;
        if (newTime <= 0) {
            timeRemaining.set(0);
            endRoundByTimeout();
        } else {
            timeRemaining.set(newTime);
        }
    }

    /**
     * Vérifie les collisions avec les explosions
     */
    private void checkExplosionCollisions() {
        // Vérifier pour le joueur 1
        int p1X = (int) Math.round(player1.getX());
        int p1Y = (int) Math.round(player1.getY());
        if (gameBoard.hasExplosion(p1X, p1Y) && player1.isAlive()) {
            player1.die();
            notifyPlayerHit(player1);
        }

        // Vérifier pour le joueur 2
        int p2X = (int) Math.round(player2.getX());
        int p2Y = (int) Math.round(player2.getY());
        if (gameBoard.hasExplosion(p2X, p2Y) && player2.isAlive()) {
            player2.die();
            notifyPlayerHit(player2);
        }
    }

    /**
     * Vérifie la collecte de power-ups
     */
    private void checkPowerUpCollection() {
        // Joueur 1
        int p1X = (int) Math.round(player1.getX());
        int p1Y = (int) Math.round(player1.getY());
        PowerUp powerUp1 = gameBoard.collectPowerUp(p1X, p1Y);
        if (powerUp1 != null) {
            player1.applyPowerUp(powerUp1.getType());
            notifyPowerUpCollected(player1, powerUp1);
        }

        // Joueur 2
        int p2X = (int) Math.round(player2.getX());
        int p2Y = (int) Math.round(player2.getY());
        PowerUp powerUp2 = gameBoard.collectPowerUp(p2X, p2Y);
        if (powerUp2 != null) {
            player2.applyPowerUp(powerUp2.getType());
            notifyPowerUpCollected(player2, powerUp2);
        }
    }

    /**
     * Vérifie si le round est terminé
     */
    private void checkRoundEnd() {
        boolean p1Alive = player1.isAlive();
        boolean p2Alive = player2.isAlive();

        if (!p1Alive && !p2Alive) {
            // Match nul
            endRound(null);
        } else if (!p1Alive) {
            // Joueur 2 gagne le round
            endRound(player2);
        } else if (!p2Alive) {
            // Joueur 1 gagne le round
            endRound(player1);
        }
    }

    /**
     * Termine le round par timeout
     */
    private void endRoundByTimeout() {
        // En cas de timeout, le joueur avec le plus de vies gagne
        if (player1.getLives() > player2.getLives()) {
            endRound(player1);
        } else if (player2.getLives() > player1.getLives()) {
            endRound(player2);
        } else {
            endRound(null); // Match nul
        }
    }

    /**
     * Termine le round actuel
     *
     * @param winner Joueur gagnant (null si match nul)
     */
    private void endRound(Player winner) {
        gameState.set(GameState.ROUND_OVER);

        if (winner != null) {
            if (winner.getPlayerNumber() == 1) {
                player1Score.set(player1Score.get() + 1);
            } else {
                player2Score.set(player2Score.get() + 1);
            }
        }

        notifyRoundEnded(winner);

        // Vérifier si la partie est terminée
        if (player1Score.get() >= roundsToWin || player2Score.get() >= roundsToWin) {
            endGame();
        }
    }

    /**
     * Continue vers le round suivant
     */
    public void continueToNextRound() {
        if (gameState.get() == GameState.ROUND_OVER) {
            currentRound.set(currentRound.get() + 1);
            startNewRound();
            gameState.set(GameState.PLAYING);
        }
    }

    /**
     * Termine la partie
     */
    private void endGame() {
        gameState.set(GameState.GAME_OVER);
        Player winner = player1Score.get() >= roundsToWin ? player1 : player2;
        notifyGameEnded(winner);
    }

    /**
     * Met le jeu en pause ou reprend
     */
    public void togglePause() {
        if (gameState.get() == GameState.PLAYING) {
            gameState.set(GameState.PAUSED);
        } else if (gameState.get() == GameState.PAUSED) {
            gameState.set(GameState.PLAYING);
        }
    }

    /**
     * Déplace un joueur
     *
     * @param playerId  Numéro du joueur (1 ou 2)
     * @param direction Direction du mouvement
     * @param deltaTime Temps écoulé
     */
    public void movePlayer(int playerId, Player.Direction direction, double deltaTime) {
        if (gameState.get() != GameState.PLAYING) return;

        Player player = (playerId == 1) ? player1 : player2;
        if (player == null || !player.isAlive()) return;

        // Sauvegarder la position actuelle
        double oldX = player.getX();
        double oldY = player.getY();

        // Calculer la nouvelle position
        double moveDistance = player.getSpeed() * deltaTime;
        double newX = oldX;
        double newY = oldY;

        switch (direction) {
            case UP -> newY -= moveDistance;
            case DOWN -> newY += moveDistance;
            case LEFT -> newX -= moveDistance;
            case RIGHT -> newX += moveDistance;
        }

        // Vérifier si le mouvement est possible
        if (canMoveTo(player, newX, newY)) {
            // Effectuer le mouvement
            player.setPosition(newX, newY);
            player.setDirection(direction);

            // Vérifier si le joueur sort de sa bombe
            checkPlayerLeavingBomb(player, oldX, oldY, newX, newY);
        }
    }

    /**
     * Vérifie si un joueur peut se déplacer à une position donnée
     * Version améliorée avec gestion des bombes
     */
    private boolean canMoveTo(Player player, double newX, double newY) {
        // Taille du joueur (hitbox)
        double playerRadius = 0.3;

        // Vérifier les limites du plateau
        if (newX - playerRadius < 0 || newX + playerRadius > GameBoard.GRID_WIDTH ||
                newY - playerRadius < 0 || newY + playerRadius > GameBoard.GRID_HEIGHT) {
            return false;
        }

        // Vérifier les 4 coins du joueur
        double[][] corners = {
                {newX - playerRadius, newY - playerRadius}, // Haut-gauche
                {newX + playerRadius, newY - playerRadius}, // Haut-droite
                {newX - playerRadius, newY + playerRadius}, // Bas-gauche
                {newX + playerRadius, newY + playerRadius}  // Bas-droite
        };

        for (double[] corner : corners) {
            int tileX = (int) Math.floor(corner[0]);
            int tileY = (int) Math.floor(corner[1]);

            // Vérifier les limites de la grille
            if (tileX < 0 || tileX >= GameBoard.GRID_WIDTH ||
                    tileY < 0 || tileY >= GameBoard.GRID_HEIGHT) {
                return false;
            }

            // Vérifier les murs
            GameBoard.TileType tile = gameBoard.getTile(tileX, tileY);
            if (tile == GameBoard.TileType.WALL || tile == GameBoard.TileType.BREAKABLE_WALL) {
                return false;
            }

            // Vérifier les bombes
            for (Bomb bomb : gameBoard.getBombs()) {
                if (bomb.getX() == tileX && bomb.getY() == tileY) {
                    // Le joueur peut traverser sa propre bombe s'il est autorisé
                    if (!bomb.canBeTraversedBy(player.getPlayerId())) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Vérifie si le joueur sort d'une bombe et met à jour les permissions
     */
    private void checkPlayerLeavingBomb(Player player, double oldX, double oldY, double newX, double newY) {
        int oldTileX = (int) Math.round(oldX);
        int oldTileY = (int) Math.round(oldY);
        int newTileX = (int) Math.round(newX);
        int newTileY = (int) Math.round(newY);

        // Si le joueur a changé de case
        if (oldTileX != newTileX || oldTileY != newTileY) {
            // Vérifier s'il y avait une bombe à l'ancienne position
            for (Bomb bomb : gameBoard.getBombs()) {
                if (bomb.getX() == oldTileX && bomb.getY() == oldTileY) {
                    // Le joueur ne peut plus traverser cette bombe
                    bomb.setCanBeTraversedBy(player.getPlayerId(), false);
                    System.out.println("Joueur " + player.getPlayerId() +
                            " a quitté sa bombe à (" + oldTileX + ", " + oldTileY + ")");
                }
            }
        }
    }

    /**
     * Place une bombe pour un joueur
     */
    public void placeBomb(int playerId) {
        Player player = (playerId == 1) ? player1 : player2;

        if (player == null || !player.isAlive() || gameState.get() != GameState.PLAYING) {
            System.out.println("Impossible de placer une bombe - État invalide");
            return;
        }

        // Vérifier si le joueur peut encore placer des bombes
        if (player.getBombsPlaced() >= player.getMaxBombs()) {
            System.out.println("Joueur " + playerId + " a atteint sa limite de bombes");
            return;
        }

        // Position actuelle du joueur
        int playerX = (int) Math.round(player.getX());
        int playerY = (int) Math.round(player.getY());

        // Utiliser la dernière direction valide si le joueur ne bouge pas
        Player.Direction direction = player.getCurrentDirection();
        if (direction == Player.Direction.NONE) {
            direction = player.getLastValidDirection();
        }

        // Calculer la position devant le joueur selon sa direction
        int bombX = playerX;
        int bombY = playerY;

        switch (direction) {
            case UP -> bombY = playerY - 1;
            case DOWN -> bombY = playerY + 1;
            case LEFT -> bombX = playerX - 1;
            case RIGHT -> bombX = playerX + 1;
            default -> {
                // Par défaut, placer sur la case du joueur
                bombX = playerX;
                bombY = playerY;
            }
        }

        System.out.println("=== PLACEMENT BOMBE ===");
        System.out.println("Joueur " + playerId + " position: (" + player.getX() + ", " + player.getY() + ")");
        System.out.println("Direction: " + direction);
        System.out.println("Case cible pour la bombe: (" + bombX + ", " + bombY + ")");

        // Vérifier que la position est valide et accessible
        boolean canPlaceAtTarget = true;

        // Vérifier les limites
        if (!gameBoard.isValidPosition(bombX, bombY)) {
            canPlaceAtTarget = false;
            System.out.println("Position hors limites!");
        }

        // Vérifier si la case est libre (pas de mur)
        if (canPlaceAtTarget) {
            GameBoard.TileType tile = gameBoard.getTile(bombX, bombY);
            if (tile == GameBoard.TileType.WALL || tile == GameBoard.TileType.BREAKABLE_WALL) {
                canPlaceAtTarget = false;
                System.out.println("Il y a un mur à cette position!");
            }
        }

        // Vérifier s'il y a déjà une bombe
        if (canPlaceAtTarget) {
            for (Bomb bomb : gameBoard.getBombs()) {
                if (bomb.getX() == bombX && bomb.getY() == bombY) {
                    canPlaceAtTarget = false;
                    System.out.println("Il y a déjà une bombe à cette position!");
                    break;
                }
            }
        }

        // Si on ne peut pas placer devant, placer sur la case du joueur
        if (!canPlaceAtTarget) {
            bombX = playerX;
            bombY = playerY;
            System.out.println("Placement sur la case du joueur à la place");

            // Vérifier qu'il n'y a pas déjà une bombe sur le joueur
            for (Bomb bomb : gameBoard.getBombs()) {
                if (bomb.getX() == bombX && bomb.getY() == bombY) {
                    System.out.println("Il y a déjà une bombe sur la case du joueur!");
                    return;
                }
            }
        }

        // Créer et placer la bombe
        Bomb bomb = new Bomb(bombX, bombY, player.getFirePower(), playerId);
        gameBoard.addBomb(bomb);
        player.incrementBombsPlaced();

        // Si la bombe est sur la case du joueur, il peut la traverser
        if (bombX == playerX && bombY == playerY) {
            bomb.setCanBeTraversedBy(playerId, true);
        }

        System.out.println("✅ Bombe placée avec succès à (" + bombX + ", " + bombY + ")!");
        System.out.println("Bombes du joueur: " + player.getBombsPlaced() + "/" + player.getMaxBombs());

        notifyBombPlaced(player, bomb);
    }

    // Méthodes pour les listeners

    /**
     * Ajoute un listener pour les événements du jeu
     */
    public void addListener(GameModelListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);
        System.out.println("Listener ajouté - Total listeners: " + listeners.size());
    }

    /**
     * Supprime un listener
     */
    public void removeListener(GameModelListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Notifie les listeners d'un changement d'état du jeu
     */
    private void notifyGameStateChanged() {
        if (listeners != null) {
            for (GameModelListener listener : listeners) {
                listener.onGameStateChanged();
            }
        }
    }

    /**
     * Notifie les listeners du début d'une partie
     */
    private void notifyGameStarted() {
        if (listeners != null) {
            for (GameModelListener listener : listeners) {
                listener.onGameStarted();
            }
        }
    }

    /**
     * Notifie les listeners du début d'un round
     */
    private void notifyRoundStarted() {
        if (listeners != null) {
            for (GameModelListener listener : listeners) {
                listener.onRoundStarted(currentRound.get());
            }
        }
    }

    /**
     * Notifie les listeners de la fin d'un round
     */
    private void notifyRoundEnded(Player winner) {
        if (listeners != null) {
            for (GameModelListener listener : listeners) {
                listener.onRoundEnded(winner);
            }
        }
    }

    /**
     * Notifie les listeners de la fin de la partie
     */
    private void notifyGameEnded(Player winner) {
        if (listeners != null) {
            for (GameModelListener listener : listeners) {
                listener.onGameEnded(winner);
            }
        }
    }

    /**
     * Notifie les listeners qu'un joueur a été touché
     */
    private void notifyPlayerHit(Player player) {
        if (listeners != null) {
            for (GameModelListener listener : listeners) {
                listener.onPlayerHit(player);
            }
        }
    }

    /**
     * Notifie les listeners qu'une bombe a été placée
     */
    private void notifyBombPlaced(Player player, Bomb bomb) {
        if (listeners != null) {
            for (GameModelListener listener : listeners) {
                listener.onBombPlaced(player, bomb);
            }
        }
    }

    /**
     * Notifie les listeners qu'un power-up a été collecté
     */
    private void notifyPowerUpCollected(Player player, PowerUp powerUp) {
        if (listeners != null) {
            for (GameModelListener listener : listeners) {
                listener.onPowerUpCollected(player, powerUp);
            }
        }
    }

    // Getters

    public GameState getGameState() {
        return gameState.get();
    }

    public ObjectProperty<GameState> gameStateProperty() {
        return gameState;
    }

    public int getCurrentRound() {
        return currentRound.get();
    }

    public IntegerProperty currentRoundProperty() {
        return currentRound;
    }

    public int getPlayer1Score() {
        return player1Score.get();
    }

    public IntegerProperty player1ScoreProperty() {
        return player1Score;
    }

    public int getPlayer2Score() {
        return player2Score.get();
    }

    public IntegerProperty player2ScoreProperty() {
        return player2Score;
    }

    public int getTimeRemaining() {
        return timeRemaining.get();
    }

    public IntegerProperty timeRemainingProperty() {
        return timeRemaining;
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    /**
     * Interface pour les écouteurs d'événements du modèle
     */
    public interface GameModelListener {
        default void onGameStarted() {
        }

        default void onRoundStarted(int roundNumber) {
        }

        default void onRoundEnded(Player winner) {
        }

        default void onGameEnded(Player winner) {
        }

        default void onPlayerHit(Player player) {
        }

        default void onBombPlaced(Player player, Bomb bomb) {
        }

        default void onPowerUpCollected(Player player, PowerUp powerUp) {
        }

        default void onGameStateChanged() {
        }
    }
}