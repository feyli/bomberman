package fr.amu.iut.bomberman.model;

import java.awt.Point;

/**
 * Représente un joueur dans le jeu Bomberman
 *
 * @author Super Bomberman Team
 * @version 1.0
 */
public class Player {

    public enum Direction {
        UP(0, -1),
        DOWN(0, 1),
        LEFT(-1, 0),
        RIGHT(1, 0),
        NONE(0, 0);

        private final int dx;
        private final int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        public int getDx() {
            return dx;
        }

        public int getDy() {
            return dy;
        }
    }

    private final int playerId;
    private String name;
    private double x, y;
    private int lives;
    private boolean alive;
    private Direction currentDirection;
    private Direction lastValidDirection;

    // Capacités du joueur
    private int maxBombs;
    private int bombsPlaced;
    private int firePower;
    private double speed;

    // Constantes
    private static final int DEFAULT_LIVES = 3;
    private static final int DEFAULT_MAX_BOMBS = 1;
    private static final int DEFAULT_FIRE_POWER = 1;
    private static final double DEFAULT_SPEED = 3.5;

    public Player(int playerId, String name, double x, double y) {
        this.playerId = playerId;
        this.name = name;
        this.x = x;
        this.y = y;
        this.lives = DEFAULT_LIVES;
        this.alive = true;
        this.currentDirection = Direction.DOWN;
        this.lastValidDirection = Direction.DOWN;
        this.maxBombs = DEFAULT_MAX_BOMBS;
        this.bombsPlaced = 0;
        this.firePower = DEFAULT_FIRE_POWER;
        this.speed = DEFAULT_SPEED;
    }

    public void reset(double newX, double newY) {
        this.x = newX;
        this.y = newY;
        this.alive = true;
        this.currentDirection = Direction.DOWN;
        this.lastValidDirection = Direction.DOWN;
        this.bombsPlaced = 0;
    }

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
    }

    public Point getPosition() {
        return new Point((int) Math.round(x), (int) Math.round(y));
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public void setMaxBombs(int maxBombs) {
        this.maxBombs = maxBombs;
    }

    public void setFirePower(int firePower) {
        this.firePower = firePower;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setBombsPlaced(int bombsPlaced) {
        this.bombsPlaced = bombsPlaced;
    }

    public void loseLife() {
        lives--;
        if (lives <= 0) {
            alive = false;
        }
    }

    public void die() {
        this.alive = false;
    }

    public void incrementBombsPlaced() {
        bombsPlaced++;
    }

    public void decrementBombsPlaced() {
        if (bombsPlaced > 0) {
            bombsPlaced--;
        }
    }

    public void increaseBombCapacity() {
        maxBombs++;
    }

    public void increaseFirePower() {
        firePower++;
    }

    public void increaseSpeed() {
        speed += 0.5;
    }

    public void addLife() {
        lives++;
    }

    public void applyPowerUp(PowerUp.Type type) {
        switch (type) {
            case BOMB_UP -> increaseBombCapacity();
            case FIRE_UP -> increaseFirePower();
            case SPEED_UP -> increaseSpeed();
            case EXTRA_LIFE -> addLife();
        }
    }

    public int getPlayerNumber() {
        return playerId;
    }

    public void setDirection(Direction direction) {
        this.currentDirection = direction;
        if (direction != Direction.NONE) {
            this.lastValidDirection = direction;
        }
    }

    public Direction getLastValidDirection() {
        return lastValidDirection;
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

    public int getBombCount() {
        return maxBombs;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}