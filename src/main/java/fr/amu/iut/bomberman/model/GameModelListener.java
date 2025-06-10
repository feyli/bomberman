package fr.amu.iut.bomberman.model;

/**
 * Interface pour les écouteurs d'événements du modèle de jeu
 *
 * @author Super Bomberman Team
 * @version 1.0
 */
public interface GameModelListener {

    /**
     * Appelé quand une nouvelle partie commence
     */
    default void onGameStarted() {
    }

    /**
     * Appelé quand un nouveau round commence
     *
     * @param roundNumber Numéro du round
     */
    default void onRoundStarted(int roundNumber) {
    }

    /**
     * Appelé quand un round se termine
     *
     * @param winner Joueur gagnant (null si match nul)
     */
    default void onRoundEnded(Player winner) {
    }

    /**
     * Appelé quand la partie se termine
     *
     * @param winner Joueur gagnant
     */
    default void onGameEnded(Player winner) {
    }

    /**
     * Appelé quand un joueur est touché par une explosion
     *
     * @param player Joueur touché
     */
    default void onPlayerHit(Player player) {
    }

    /**
     * Appelé quand une bombe est placée
     *
     * @param player Joueur qui a placé la bombe
     * @param bomb   Bombe placée
     */
    default void onBombPlaced(Player player, Bomb bomb) {
    }

    /**
     * Appelé quand un power-up est collecté
     *
     * @param player  Joueur qui a collecté
     * @param powerUp Power-up collecté
     */
    default void onPowerUpCollected(Player player, PowerUp powerUp) {
    }

    /**
     * Appelé quand l'état du jeu change
     */
    default void onGameStateChanged() {
    }
}