package fr.amu.iut.bomberman;

public class Explosion {
    private final int x;
    private final int y;
    private int duration;

    public Explosion(int x, int y) {
        this.x = x;
        this.y = y;
        this.duration = Constants.EXPLOSION_DURATION;
    }

    public boolean tick() {
        duration -= Constants.GAME_LOOP_DELAY;
        return duration <= 0;
    }

    public boolean isActive() {
        return duration > 0;
    }

    // Getters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDuration() {
        return duration;
    }
}
