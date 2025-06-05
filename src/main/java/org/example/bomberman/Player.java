package org.example.bomberman;

import java.awt.*;

public class Player {
    private int id;
    private String name;
    private int x, y;
    private int lives;
    private int bombCount;
    private int bombRange;
    private int speed;
    private boolean isAlive;
    private Direction lastDirection;

    public Player(int id, String name, int startX, int startY) {
        this.id = id;
        this.name = name;
        this.x = startX;
        this.y = startY;
        this.lives = Constants.DEFAULT_LIVES;
        this.bombCount = Constants.DEFAULT_BOMB_COUNT;
        this.bombRange = Constants.DEFAULT_BOMB_RANGE;
        this.speed = Constants.DEFAULT_PLAYER_SPEED;
        this.isAlive = true;
        this.lastDirection = Direction.DOWN;
    }

    public void move(Direction direction, GameBoard board) {
        if (!isAlive) return;

        int newX = x + direction.getDx();
        int newY = y + direction.getDy();

        if (board.isValidMove(newX, newY)) {
            x = newX;
            y = newY;
            lastDirection = direction;

            // Vérifier s'il y a un power-up à collecter
            Cell cell = board.getCellAt(x, y);
            if (cell.getPowerUp() != null) {
                collectPowerUp(cell.getPowerUp());
                cell.setPowerUp(null);
                // Retirer le power-up de la liste du board
                board.getPowerUps().remove(cell.getPowerUp());
            }
        }
    }

    public boolean placeBomb(GameBoard board) {
        if (!isAlive || board.getActiveBombsCount(this) >= bombCount) {
            return false;
        }

        Cell cell = board.getCellAt(x, y);
        if (cell.getType() != CellType.EMPTY) {
            return false;
        }

        Bomb bomb = new Bomb(x, y, this, bombRange);
        board.placeBomb(bomb);
        return true;
    }

    public void takeDamage() {
        lives--;
        if (lives <= 0) {
            isAlive = false;
        }
    }

    public void collectPowerUp(PowerUp powerUp) {
        switch (powerUp.getType()) {
            case EXTRA_BOMB:
                bombCount += powerUp.getValue();
                break;
            case INCREASED_RANGE:
                bombRange += powerUp.getValue();
                break;
            case SPEED_BOOST:
                speed += powerUp.getValue();
                break;
            case EXTRA_LIFE:
                lives += powerUp.getValue();
                break;
        }
    }

    public Point getPosition() {
        return new Point(x, y);
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getLives() {
        return lives;
    }

    public int getBombCount() {
        return bombCount;
    }

    public int getBombRange() {
        return bombRange;
    }

    public int getSpeed() {
        return speed;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public Direction getLastDirection() {
        return lastDirection;
    }

    // Setters pour les tests
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setLives(int lives) {
        this.lives = lives;
        this.isAlive = lives > 0;
    }
}
