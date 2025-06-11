package fr.amu.iut.bomberman.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitaires pour la classe PowerUp
 */
public class PowerUpTest {

    private PowerUp bombUpPowerUp;
    private PowerUp fireUpPowerUp;
    private static final double DELTA = 0.001; // Delta pour comparaisons à virgule flottante

    @BeforeEach
    void setUp() {
        bombUpPowerUp = new PowerUp(3, 4, PowerUp.Type.BOMB_UP);
        fireUpPowerUp = new PowerUp(5, 6, PowerUp.Type.FIRE_UP);
    }

    @Test
    void testPowerUpInitialization() {
        assertEquals(3, bombUpPowerUp.getX());
        assertEquals(4, bombUpPowerUp.getY());
        assertEquals(PowerUp.Type.BOMB_UP, bombUpPowerUp.getType());
        assertEquals(0, bombUpPowerUp.getAnimationTimer(), DELTA);

        assertEquals(5, fireUpPowerUp.getX());
        assertEquals(6, fireUpPowerUp.getY());
        assertEquals(PowerUp.Type.FIRE_UP, fireUpPowerUp.getType());
        assertEquals(0, fireUpPowerUp.getAnimationTimer(), DELTA);
    }

    @Test
    void testUpdate() {
        double deltaTime = 0.5;

        // Initial animation timer should be 0
        assertEquals(0, bombUpPowerUp.getAnimationTimer(), DELTA);

        // Update animation timer
        bombUpPowerUp.update(deltaTime);
        assertEquals(deltaTime, bombUpPowerUp.getAnimationTimer(), DELTA);

        // Update again
        bombUpPowerUp.update(deltaTime);
        assertEquals(deltaTime * 2, bombUpPowerUp.getAnimationTimer(), DELTA);
    }

    @Test
    void testGetAnimationFrame() {
        // Animation frame should initially be 0
        assertEquals(0, bombUpPowerUp.getAnimationFrame());

        // Update to get to frame 1
        bombUpPowerUp.update(0.26); // 0.26 * 4 = 1.04 => frame 1
        assertEquals(1, bombUpPowerUp.getAnimationFrame());

        // Update to get to frame 2
        bombUpPowerUp.update(0.25); // (0.26 + 0.25) * 4 = 2.04 => frame 2
        assertEquals(2, bombUpPowerUp.getAnimationFrame());

        // Update to get to frame 3
        bombUpPowerUp.update(0.25); // (0.26 + 0.25 + 0.25) * 4 = 3.04 => frame 3
        assertEquals(3, bombUpPowerUp.getAnimationFrame());

        // Update to cycle back to frame 0
        bombUpPowerUp.update(0.25); // (0.26 + 0.25 + 0.25 + 0.25) * 4 = 4.04 => frame 0
        assertEquals(0, bombUpPowerUp.getAnimationFrame());
    }

    @Test
    void testPowerUpTypeNames() {
        assertEquals("Bomb Up", PowerUp.Type.BOMB_UP.getName());
        assertEquals("Fire Up", PowerUp.Type.FIRE_UP.getName());
        assertEquals("Speed Up", PowerUp.Type.SPEED_UP.getName());
        assertEquals("Kick", PowerUp.Type.KICK.getName());
        assertEquals("Remote", PowerUp.Type.REMOTE_CONTROL.getName());
        assertEquals("Life", PowerUp.Type.EXTRA_LIFE.getName());
    }

    @Test
    void testPowerUpTypeDescriptions() {
        assertEquals("Augmente le nombre de bombes", PowerUp.Type.BOMB_UP.getDescription());
        assertEquals("Augmente la portée des explosions", PowerUp.Type.FIRE_UP.getDescription());
        assertEquals("Augmente la vitesse de déplacement", PowerUp.Type.SPEED_UP.getDescription());
        assertEquals("Permet de pousser les bombes", PowerUp.Type.KICK.getDescription());
        assertEquals("Contrôle à distance des bombes", PowerUp.Type.REMOTE_CONTROL.getDescription());
        assertEquals("Vie supplémentaire", PowerUp.Type.EXTRA_LIFE.getDescription());
    }
}
