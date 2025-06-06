package fr.amu.iut.bomberman;

import java.awt.*;

public class Player {
    private final int id;
    private final String name;
    private int x, y;
    private int lives;
    private int bombCount;
    private int bombRange;
    private int speed;
    private boolean isAlive;
    private Direction lastDirection;
    private final Point initialPos; // Initial position for respawn
    private boolean spawnProtected; // Spawn protection flag

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
        this.initialPos = new Point(x, y);
        enableSpawnProtection();
    }

    public void enableSpawnProtection() {
        this.spawnProtected = true;
        // Disable spawn protection after 3 seconds
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                spawnProtected = false;
            } catch (InterruptedException e) {
                System.out.println("Erreur lors de la réactivation de la protection de spawn :\n\n" + e.getMessage());
            }
        }).start();
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

    public void placeBomb(GameBoard board) {
        if (!isAlive || board.getActiveBombsCount(this) >= bombCount) {
            return;
        }

        Cell cell = board.getCellAt(x, y);
        if (cell.getType() != CellType.EMPTY) {
            return;
        }

        Bomb bomb = new Bomb(x, y, this, bombRange);
        board.placeBomb(bomb);
    }

    public void takeDamage() {
        if (!isAlive || spawnProtected) {
            return; // Do not take damage if dead or spawn protection is ac tive
        }
        enableSpawnProtection();
        lives--;
        if (lives <= 0) {
            isAlive = false;
            return;
        }
        setPosition(initialPos.x, initialPos.y);
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
