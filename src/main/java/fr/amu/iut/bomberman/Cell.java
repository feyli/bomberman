package fr.amu.iut.bomberman;

public class Cell {
    private final int x;
    private final int y;
    private CellType type;
    private boolean isDestructible;
    private PowerUp powerUp;

    public Cell(int x, int y, CellType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.isDestructible = (type == CellType.DESTRUCTIBLE_WALL);
    }

    public boolean isEmpty() {
        return type == CellType.EMPTY && powerUp == null;
    }

    public boolean canWalkThrough() {
        return type == CellType.EMPTY || type == CellType.EXPLOSION;
    }

    public void destroy() {
        if (isDestructible) {
            type = CellType.EMPTY;
            isDestructible = false;
        }
    }

    // Getters et Setters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public CellType getType() {
        return type;
    }

    public void setType(CellType type) {
        this.type = type;
    }

    public boolean isDestructible() {
        return isDestructible;
    }

    public PowerUp getPowerUp() {
        return powerUp;
    }

    public void setPowerUp(PowerUp powerUp) {
        this.powerUp = powerUp;
    }
}
