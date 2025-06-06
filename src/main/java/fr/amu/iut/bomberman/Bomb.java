package fr.amu.iut.bomberman;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Bomb {
    private final int x;
    private final int y;
    private final Player owner;
    private int timer;
    private final int explosionRange;
    private boolean hasExploded;

    public Bomb(int x, int y, Player owner, int explosionRange) {
        this.x = x;
        this.y = y;
        this.owner = owner;
        this.explosionRange = explosionRange;
        this.timer = Constants.BOMB_TIMER;
        this.hasExploded = false;
    }

    public boolean tick() {
        timer -= Constants.GAME_LOOP_DELAY;
        return timer <= 0;
    }

    public List<Point> getExplosionCells(GameBoard board) {
        List<Point> explosionCells = new ArrayList<>();
        explosionCells.add(new Point(x, y)); // Centre de l'explosion

        // Explosion dans les 4 directions
        for (fr.amu.iut.bomberman.Direction dir : fr.amu.iut.bomberman.Direction.values()) {
            for (int i = 1; i <= explosionRange; i++) {
                int newX = x + dir.getDx() * i;
                int newY = y + dir.getDy() * i;

                if (!board.isInBounds(newX, newY)) break;

                Cell cell = board.getCellAt(newX, newY);
                explosionCells.add(new Point(newX, newY));

                // L'explosion s'arrête sur les murs
                if (cell.getType() == fr.amu.iut.bomberman.CellType.WALL ||
                        cell.getType() == fr.amu.iut.bomberman.CellType.DESTRUCTIBLE_WALL) {
                    break;
                }
            }
        }

        return explosionCells;
    }

    public void explode() {
        hasExploded = true;
    }

    // Getters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Player getOwner() {
        return owner;
    }

    public int getTimer() {
        return timer;
    }

    public int getExplosionRange() {
        return explosionRange;
    }

    public boolean hasExploded() {
        return hasExploded;
    }
}
