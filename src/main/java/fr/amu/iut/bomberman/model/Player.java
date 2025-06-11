package fr.amu.iut.bomberman.model;

import fr.amu.iut.bomberman.utils.Direction;

/**
 * Représente un joueur dans le jeu Bomberman
 *
 * @author Super Bomberman Team
 * @version 1.0
 */
public class Player {

    private final int playerId;
    private String name;
    private double x, y;
    private int lives;
    private boolean alive;
    private Direction currentDirection;
    private Direction lastValidDirection; // Dernière direction valide pour le placement des bombes

    // Capacités du joueur
    private int maxBombs;
    private int bombsPlaced;
    private int firePower;
    private double speed;

    // Invincibilité
    private boolean isInvincible; // Invincibilité activée de manière éphémère

    // Constantes
    private static final int DEFAULT_LIVES = 3;
    private static final int DEFAULT_MAX_BOMBS = 1;
    private static final int DEFAULT_FIRE_POWER = 1;
    private static final double DEFAULT_SPEED = 3.5; // Augmenté pour un meilleur gameplay

    // Score du joueur
    private int score;

    /**
     * Constructeur
     */
    public Player(int playerId, String name, double x, double y) {
        this.playerId = playerId;
        this.name = name;
        this.x = x;
        this.y = y;
        this.lives = DEFAULT_LIVES;
        this.alive = true;
        this.currentDirection = Direction.DOWN;
        this.lastValidDirection = Direction.DOWN;

        // Capacités initiales
        this.maxBombs = DEFAULT_MAX_BOMBS;
        this.bombsPlaced = 0;
        this.firePower = DEFAULT_FIRE_POWER;
        this.speed = DEFAULT_SPEED;

        enableTemporaryInvincibility();

        System.out.println("Joueur " + playerId + " créé: " + name + " à (" + x + ", " + y + ")");
    }

    /**
     * Remet le joueur à sa position initiale pour un nouveau round
     */
    public void reset(double newX, double newY) {
        this.x = newX;
        this.y = newY;
        this.alive = true;
        this.currentDirection = Direction.DOWN;
        this.lastValidDirection = Direction.DOWN;
        this.bombsPlaced = 0;
        // Les capacités (maxBombs, firePower, speed) sont conservées entre les rounds

        System.out.println("Joueur " + playerId + " réinitialisé à (" + x + ", " + y + ")");
    }

    /**
     * Déplace le joueur dans une direction
     */
    public void move(Direction direction, double deltaTime) {
        if (!alive) return;

        this.currentDirection = direction;

        double moveDistance = speed * deltaTime;

        switch (direction) {
            case UP -> y -= moveDistance;
            case DOWN -> y += moveDistance;
            case LEFT -> x -= moveDistance;
            case RIGHT -> x += moveDistance;
        }
        // Suppression de l'appel récursif qui causait le StackOverflowError
        // move(direction, 1.0);  // Cette ligne crée une récursion infinie!
    }

    /**
     * Définit la position du joueur
     */
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Définit la position X du joueur
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Définit la position Y du joueur
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Le joueur perd une vie
     */
    public void loseLife() {
        if (!alive || isInvincible) {
            System.out.println("Joueur " + playerId + " ne peut pas perdre de vie (mort ou invincible)!");
            return;
        }
        lives--;
        if (lives <= 0) {
            alive = false;
        }
        enableTemporaryInvincibility();
        System.out.println("Joueur " + playerId + " perd une vie! Vies restantes: " + lives);
    }

    /**
     * Le joueur meurt
     */
    public void die() {
        this.alive = false;
        System.out.println("Joueur " + playerId + " est mort!");
    }

    /**
     * Incrémente le nombre de bombes placées
     */
    public void incrementBombsPlaced() {
        bombsPlaced++;
    }

    /**
     * Décrémente le nombre de bombes placées (quand une bombe explose)
     */
    public void decrementBombsPlaced() {
        if (bombsPlaced > 0) {
            bombsPlaced--;
        }
    }

    /**
     * Améliore la capacité de bombes
     */
    public void increaseBombCapacity() {
        maxBombs++;
        System.out.println("Joueur " + playerId + " - Bomb Up! Max bombes: " + maxBombs);
    }

    /**
     * Améliore la puissance de feu
     */
    public void increaseFirePower() {
        firePower++;
        System.out.println("Joueur " + playerId + " - Fire Up! Puissance: " + firePower);
    }

    /**
     * Améliore la vitesse
     */
    public void increaseSpeed() {
        speed += 0.5;
        System.out.println("Joueur " + playerId + " - Speed Up! Vitesse: " + speed);
    }

    /**
     * Ajoute une vie
     */
    public void addLife() {
        lives++;
        System.out.println("Joueur " + playerId + " - Extra Life! Vies: " + lives);
    }

    /**
     * Applique un power-up au joueur
     */
    public void applyPowerUp(PowerUp.Type type) {
        switch (type) {
            case BOMB_UP -> increaseBombCapacity();
            case FIRE_UP -> increaseFirePower();
            case SPEED_UP -> increaseSpeed();
            case EXTRA_LIFE -> addLife();
            // Les autres types peuvent être ajoutés plus tard
        }
    }

    /**
     * Retourne le numéro du joueur (alias pour getPlayerId)
     */
    public int getPlayerNumber() {
        return playerId;
    }

    /**
     * Définit la direction courante du joueur
     */
    public void setDirection(Direction direction) {
        this.currentDirection = direction;
        // Sauvegarder la dernière direction valide (non NONE)
        if (direction != Direction.NONE) {
            this.lastValidDirection = direction;
        }
    }

    public void enableTemporaryInvincibility() {
        System.out.println("Joueur " + playerId + " est temporairement invincible!");
        this.isInvincible = true;

        // Désactiver l'invincibilité après 3 secondes
        new Thread(() -> {
            try {
                Thread.sleep(3000); // 3 secondes d'invincibilité
                this.isInvincible = false;
                System.out.println("Joueur " + playerId + " n'est plus invincible!");
            } catch (InterruptedException e) {
                System.out.println("Erreur lors de la désactivation de l'invincibilité du joueur " + playerId + ": " + e.getMessage());
            }
        }).start();
    }

    /**
     * Obtient la dernière direction valide du joueur
     */
    public Direction getLastValidDirection() {
        return lastValidDirection;
    }

    /**
     * Retourne le score du joueur
     *
     * @return score actuel
     */
    public int getScore() {
        return score;
    }

    /**
     * Ajoute des points au score du joueur
     *
     * @param points Points à ajouter
     */
    public void addScore(int points) {
        this.score += points;
    }

    // Getters
    public int getPlayerId() {
        return playerId;
    }

    public String getName() {
        return name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getLives() {
        return lives;
    }

    public boolean isAlive() {
        return alive;
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public int getMaxBombs() {
        return maxBombs;
    }

    public int getBombsPlaced() {
        return bombsPlaced;
    }

    public int getFirePower() {
        return firePower;
    }

    public double getSpeed() {
        return speed;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    /**
     * Vérifie si le joueur peut placer une bombe
     */
    public boolean canPlaceBomb() {
        return alive && bombsPlaced < maxBombs;
    }

    /**
     * Place une bombe et met à jour le compteur de bombes placées
     */
    public void placeBomb() {
        if (canPlaceBomb()) {
            incrementBombsPlaced();
            System.out.println("Bombe placée par le joueur " + playerId + "! Total bombes placées: " + bombsPlaced);
        } else {
            System.out.println("Le joueur " + playerId + " ne peut pas placer de bombe (limite atteinte ou joueur mort)!");
        }
    }
}