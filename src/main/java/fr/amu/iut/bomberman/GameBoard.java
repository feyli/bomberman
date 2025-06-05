package fr.amu.iut.bomberman;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameBoard {
    private final int width;
    private final int height;
    private final Cell[][] grid;
    private final List<Bomb> activeBombs;
    private final List<Explosion> activeExplosions;
    private final List<PowerUp> powerUps;
    private final Random random;

    public GameBoard() {
        this.width = Constants.BOARD_WIDTH;
        this.height = Constants.BOARD_HEIGHT;
        this.grid = new Cell[width][height];
        this.activeBombs = new ArrayList<>();
        this.activeExplosions = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.random = new Random();

        initializeBoard();
    }

    private void initializeBoard() {
        // Initialiser toutes les cases
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = new Cell(x, y, CellType.EMPTY);
            }
        }

        // Placer les murs fixes (bordures et pattern)
        placeWalls();

        // Placer les murs destructibles
        placeDestructibleWalls();
    }

    private void placeWalls() {
        // Bordures
        for (int x = 0; x < width; x++) {
            grid[x][0].setType(CellType.WALL);
            grid[x][height - 1].setType(CellType.WALL);
        }
        for (int y = 0; y < height; y++) {
            grid[0][y].setType(CellType.WALL);
            grid[width - 1][y].setType(CellType.WALL);
        }

        // Pattern de murs internes (tous les 2 cases)
        for (int x = 2; x < width - 1; x += 2) {
            for (int y = 2; y < height - 1; y += 2) {
                grid[x][y].setType(CellType.WALL);
            }
        }
    }

    private void placeDestructibleWalls() {
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                Cell cell = grid[x][y];

                // Ne pas placer sur les murs fixes ou les zones de spawn
                if (cell.getType() == CellType.WALL || isSpawnArea(x, y)) {
                    continue;
                }

                // Placer aléatoirement des murs destructibles
                if (random.nextDouble() < Constants.DESTRUCTIBLE_WALL_PROBABILITY) {
                    cell.setType(CellType.DESTRUCTIBLE_WALL);
                }
            }
        }
    }

    private boolean isSpawnArea(int x, int y) {
        // Zones de spawn des joueurs (coins + 1 case autour)
        return (x <= 2 && y <= 2) ||           // Coin haut-gauche
                (x >= width - 3 && y <= 2) ||   // Coin haut-droite
                (x <= 2 && y >= height - 3) ||  // Coin bas-gauche
                (x >= width - 3 && y >= height - 3); // Coin bas-droite
    }

    public Cell getCellAt(int x, int y) {
        if (isInBounds(x, y)) {
            return grid[x][y];
        }
        return null;
    }

    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public boolean isValidMove(int x, int y) {
        if (!isInBounds(x, y)) return false;

        Cell cell = getCellAt(x, y);
        return cell.canWalkThrough();
    }

    public void placeBomb(Bomb bomb) {
        activeBombs.add(bomb);
        Cell cell = getCellAt(bomb.getX(), bomb.getY());
        cell.setType(CellType.BOMB);
    }

    public void update() {
        updateBombs();
        updateExplosions();
    }

    private void updateBombs() {
        Iterator<Bomb> bombIterator = activeBombs.iterator();

        while (bombIterator.hasNext()) {
            Bomb bomb = bombIterator.next();

            if (bomb.tick()) {
                explodeBomb(bomb);
                bombIterator.remove();
            }
        }
    }

    private void explodeBomb(Bomb bomb) {
        bomb.explode();

        // Remettre la case de la bombe à vide
        Cell bombCell = getCellAt(bomb.getX(), bomb.getY());
        bombCell.setType(CellType.EMPTY);

        // Créer les explosions
        List<Point> explosionCells = bomb.getExplosionCells(this);
        for (Point point : explosionCells) {
            Cell cell = getCellAt(point.x, point.y);

            // Détruire les murs destructibles et créer des power-ups
            if (cell.getType() == CellType.DESTRUCTIBLE_WALL) {
                cell.destroy();

                // Chance de créer un power-up
                if (random.nextDouble() < Constants.POWERUP_DROP_PROBABILITY) {
                    PowerUpType[] types = PowerUpType.values();
                    PowerUpType randomType = types[random.nextInt(types.length)];
                    PowerUp powerUp = new PowerUp(point.x, point.y, randomType);
                    cell.setPowerUp(powerUp);
                    powerUps.add(powerUp);
                }
            }

            // Créer l'explosion
            if (cell.canWalkThrough() || cell.getType() == CellType.DESTRUCTIBLE_WALL) {
                cell.setType(CellType.EXPLOSION);
                activeExplosions.add(new Explosion(point.x, point.y));
            }
        }
    }

    private void updateExplosions() {
        Iterator<Explosion> explosionIterator = activeExplosions.iterator();

        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();

            if (explosion.tick()) {
                // Remettre la case à vide
                Cell cell = getCellAt(explosion.getX(), explosion.getY());
                cell.setType(CellType.EMPTY);
                explosionIterator.remove();
            }
        }
    }

    public List<Point> getExplosionPositions() {
        List<Point> positions = new ArrayList<>();
        for (Explosion explosion : activeExplosions) {
            positions.add(new Point(explosion.getX(), explosion.getY()));
        }
        return positions;
    }

    public boolean isPlayerInExplosion(Player player) {
        for (Explosion explosion : activeExplosions) {
            if (explosion.getX() == player.getX() && explosion.getY() == player.getY()) {
                return true;
            }
        }
        return false;
    }

    public int getActiveBombsCount(Player player) {
        int count = 0;
        for (Bomb bomb : activeBombs) {
            if (bomb.getOwner() == player) {
                count++;
            }
        }
        return count;
    }

    public Point[] getPlayerSpawnPositions() {
        return new Point[]{
                new Point(1, 1),                    // Joueur 1
                new Point(width - 2, 1),            // Joueur 2
                new Point(1, height - 2),           // Joueur 3
                new Point(width - 2, height - 2)    // Joueur 4
        };
    }

    // Getters
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Cell[][] getGrid() {
        return grid;
    }

    public List<Bomb> getActiveBombs() {
        return activeBombs;
    }

    public List<Explosion> getActiveExplosions() {
        return activeExplosions;
    }

    public List<PowerUp> getPowerUps() {
        return powerUps;
    }
}
