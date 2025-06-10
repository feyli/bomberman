package fr.amu.iut.bomberman.model;

/**
 * Classe représentant un power-up dans le jeu
 *
 * @author Super Bomberman Team
 * @version 1.0
 */
public class PowerUp {

    /**
     * Types de power-ups disponibles
     */
    public enum Type {
        BOMB_UP("Bomb Up", "Augmente le nombre de bombes"),
        FIRE_UP("Fire Up", "Augmente la portée des explosions"),
        SPEED_UP("Speed Up", "Augmente la vitesse de déplacement"),
        KICK("Kick", "Permet de pousser les bombes"),
        REMOTE_CONTROL("Remote", "Contrôle à distance des bombes"),
        EXTRA_LIFE("Life", "Vie supplémentaire");

        private final String name;
        private final String description;

        Type(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }

    private final int x;
    private final int y;
    private final Type type;
    private double animationTimer;

    /**
     * Constructeur du power-up
     *
     * @param x    Position X sur la grille
     * @param y    Position Y sur la grille
     * @param type Type de power-up
     */
    public PowerUp(int x, int y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.animationTimer = 0;
    }

    /**
     * Met à jour l'animation du power-up
     *
     * @param deltaTime Temps écoulé
     */
    public void update(double deltaTime) {
        animationTimer += deltaTime;
    }

    /**
     * Obtient le frame d'animation actuel
     *
     * @return Indice du frame (0-3)
     */
    public int getAnimationFrame() {
        return (int) (animationTimer * 4) % 4;
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

    public double getAnimationTimer() {
        return animationTimer;
    }
}