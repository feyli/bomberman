package org.example.bomberman;

public class PowerUp {
    private int x, y;
    private PowerUpType type;
    private int value;

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
