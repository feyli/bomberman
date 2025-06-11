package fr.amu.iut.bomberman.model;

/**
 * Représente une explosion dans le jeu
 *
 * @author Groupe_3_6
 * @version 1.0
 */
public class Explosion {

    public enum Type {
        CENTER,         // Centre de l'explosion
        HORIZONTAL,     // Ligne horizontale
        VERTICAL,       // Ligne verticale
        END_UP,         // Fin vers le haut
        END_DOWN,       // Fin vers le bas
        END_LEFT,       // Fin vers la gauche
        END_RIGHT       // Fin vers la droite
    }

    private final int x;
    private final int y;
    private final Type type;
    private double timeRemaining;

    public static final double DURATION = 0.5; // 0.5 secondes

    /**
     * Constructeur
     */
    public Explosion(int x, int y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.timeRemaining = DURATION;
    }

    /**
     * Met à jour l'explosion
     */
    public void update(double deltaTime) {
        timeRemaining -= deltaTime;
    }

    /**
     * Vérifie si l'explosion est terminée
     */
    public boolean isFinished() {
        return timeRemaining <= 0;
    }

    /**
     * Retourne l'intensité de l'explosion (1.0 = début, 0.0 = fin)
     */
    public double getIntensity() {
        return Math.max(0, timeRemaining / DURATION);
    }

    // Getters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Type getType() {
        return type;
    }

    public double getTimeRemaining() {
        return timeRemaining;
    }
}