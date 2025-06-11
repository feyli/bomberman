package fr.amu.iut.bomberman.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitaires pour la classe Bomb
 */
public class BombTest {

    private Bomb bomb;
    private static final double DELTA = 0.001; // Delta pour comparaisons à virgule flottante
    private static final int OWNER_ID = 1;

    @BeforeEach
    void setUp() {
        // Créer une bombe à la position (5, 7) avec une puissance de feu de 2, appartenant au joueur 1
        bomb = new Bomb(5, 7, 2, OWNER_ID);
    }

    @Test
    void testBombInitialization() {
        assertEquals(5, bomb.getX());
        assertEquals(7, bomb.getY());
        assertEquals(2, bomb.getFirePower());
        assertEquals(OWNER_ID, bomb.getOwnerId());
        assertEquals(1.0, bomb.getTimePercentage(), DELTA);
        assertFalse(bomb.shouldExplode());
    }

    @Test
    void testBombUpdate() {
        // Bomb should initially have EXPLOSION_TIME remaining
        assertEquals(1.0, bomb.getTimePercentage(), DELTA);

        // Update with half the explosion time passed
        bomb.update(Bomb.EXPLOSION_TIME / 2);
        assertEquals(0.5, bomb.getTimePercentage(), DELTA);
        assertFalse(bomb.shouldExplode());

        // Update with remaining time
        bomb.update(Bomb.EXPLOSION_TIME / 2);
        assertEquals(0.0, bomb.getTimePercentage(), DELTA);
        assertTrue(bomb.shouldExplode());
    }

    @Test
    void testForceExplode() {
        // Initially the bomb should not explode
        assertFalse(bomb.shouldExplode());

        // Force the bomb to explode
        bomb.forceExplode();

        // Now it should explode
        assertTrue(bomb.shouldExplode());
        assertEquals(0.0, bomb.getTimePercentage(), DELTA);
    }

    @Test
    void testPlayerTraversal() {
        // Initially players shouldn't be able to traverse the bomb
        assertFalse(bomb.canBeTraversedBy(OWNER_ID));
        assertFalse(bomb.canBeTraversedBy(2));

        // Allow player 1 to traverse
        bomb.setCanBeTraversedBy(OWNER_ID, true);
        assertTrue(bomb.canBeTraversedBy(OWNER_ID));
        assertFalse(bomb.canBeTraversedBy(2));

        // Allow player 2 to traverse
        bomb.setCanBeTraversedBy(2, true);
        assertTrue(bomb.canBeTraversedBy(OWNER_ID));
        assertTrue(bomb.canBeTraversedBy(2));

        // Disallow player 1 from traversing
        bomb.setCanBeTraversedBy(OWNER_ID, false);
        assertFalse(bomb.canBeTraversedBy(OWNER_ID));
        assertTrue(bomb.canBeTraversedBy(2));
    }

    @Test
    void testTooMuchTimePassed() {
        // Try to update with more than the explosion time
        bomb.update(Bomb.EXPLOSION_TIME * 2);

        // The bomb should be ready to explode
        assertTrue(bomb.shouldExplode());
        assertEquals(0.0, bomb.getTimePercentage(), DELTA);
    }
}
