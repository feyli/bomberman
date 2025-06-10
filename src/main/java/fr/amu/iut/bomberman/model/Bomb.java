package fr.amu.iut.bomberman.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Représente une bombe dans le jeu
 *
 * @author Super Bomberman Team
 * @version 1.0
 */
public class Bomb {

    private final int x;
    private final int y;
    private final int firePower;
    private final int ownerId;
    private double timeRemaining;
    private boolean forceExplode;

    // Joueurs qui peuvent traverser cette bombe
    private Set<Integer> canTraverse;

    public static final double EXPLOSION_TIME = 3.0; // 3 secondes

    /**
     * Constructeur
     */
    public Bomb(int x, int y, int firePower, int ownerId) {
        this.x = x;
        this.y = y;
        this.firePower = firePower;
        this.ownerId = ownerId;
        this.timeRemaining = EXPLOSION_TIME;
        this.forceExplode = false;
        this.canTraverse = new HashSet<>();

        System.out.println("Nouvelle bombe créée à (" + x + ", " + y + ") par joueur " + ownerId);
    }

    /**
     * Met à jour la bombe
     */
    public void update(double deltaTime) {
        if (!forceExplode) {
            timeRemaining -= deltaTime;
        }
    }

    /**
     * Vérifie si la bombe doit exploser
     */
    public boolean shouldExplode() {
        return timeRemaining <= 0 || forceExplode;
    }

    /**
     * Force l'explosion de la bombe (réaction en chaîne)
     */
    public void forceExplode() {
        this.forceExplode = true;
        this.timeRemaining = 0;
        System.out.println("Bombe à (" + x + ", " + y + ") forcée d'exploser!");
    }

    /**
     * Retourne le pourcentage de temps restant (1.0 = vient d'être placée, 0.0 = va exploser)
     */
    public double getTimePercentage() {
        return Math.max(0, timeRemaining / EXPLOSION_TIME);
    }

    /**
     * Définit si un joueur peut traverser cette bombe
     */
    public void setCanBeTraversedBy(int playerId, boolean canTraverse) {
        if (canTraverse) {
            this.canTraverse.add(playerId);
            System.out.println("Joueur " + playerId + " peut traverser la bombe à (" + x + ", " + y + ")");
        } else {
            this.canTraverse.remove(playerId);
            System.out.println("Joueur " + playerId + " ne peut plus traverser la bombe à (" + x + ", " + y + ")");
        }
    }

    /**
     * Vérifie si un joueur peut traverser cette bombe
     */
    public boolean canBeTraversedBy(int playerId) {
        return canTraverse.contains(playerId);
    }

    // Getters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getFirePower() {
        return firePower;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public double getTimeRemaining() {
        return timeRemaining;
    }
}