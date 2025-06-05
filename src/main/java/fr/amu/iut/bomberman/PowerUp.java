package fr.amu.iut.bomberman;

public class PowerUp {
    private final int x;
    private final int y;
    private final PowerUpType type;
    private final int value;

    public PowerUp(int x, int y, PowerUpType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.value = type.getValue();
    }

    public void apply(Player player) {
        player.collectPowerUp(this);
    }

    // Getters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public PowerUpType getType() {
        return type;
    }

    public int getValue() {
        return value;
    }
}
