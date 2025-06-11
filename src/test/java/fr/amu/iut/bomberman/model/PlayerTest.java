package fr.amu.iut.bomberman.model;

import fr.amu.iut.bomberman.utils.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitaires pour la classe Player
 */
public class PlayerTest {

    private Player player;
    private static final double DELTA = 0.001; // Delta pour comparaisons à virgule flottante

    @BeforeEach
    void setUp() {
        player = new Player(1, "TestPlayer", 2.0, 3.0);
    }

    @Test
    void testPlayerInitialization() {
        assertEquals(1, player.getPlayerId());
        assertEquals("TestPlayer", player.getName());
        assertEquals(2.0, player.getX(), DELTA);
        assertEquals(3.0, player.getY(), DELTA);
        assertEquals(3, player.getLives());
        assertTrue(player.isAlive());
        assertEquals(Direction.DOWN, player.getCurrentDirection());
        assertEquals(1, player.getMaxBombs());
        assertEquals(0, player.getBombsPlaced());
        assertEquals(1, player.getFirePower());
        assertTrue(player.getIsInvincible());
    }

    @Test
    void testMove() {
        double deltaTime = 0.5;
        double expectedMoveDistance = player.getSpeed() * deltaTime;

        // Test moving up
        player.move(Direction.UP, deltaTime);
        assertEquals(2.0, player.getX(), DELTA);
        assertEquals(3.0 - expectedMoveDistance, player.getY(), DELTA);
        assertEquals(Direction.UP, player.getCurrentDirection());

        // Reset position and test moving down
        player.setPosition(2.0, 3.0);
        player.move(Direction.DOWN, deltaTime);
        assertEquals(2.0, player.getX(), DELTA);
        assertEquals(3.0 + expectedMoveDistance, player.getY(), DELTA);
        assertEquals(Direction.DOWN, player.getCurrentDirection());

        // Reset position and test moving left
        player.setPosition(2.0, 3.0);
        player.move(Direction.LEFT, deltaTime);
        assertEquals(2.0 - expectedMoveDistance, player.getX(), DELTA);
        assertEquals(3.0, player.getY(), DELTA);
        assertEquals(Direction.LEFT, player.getCurrentDirection());

        // Reset position and test moving right
        player.setPosition(2.0, 3.0);
        player.move(Direction.RIGHT, deltaTime);
        assertEquals(2.0 + expectedMoveDistance, player.getX(), DELTA);
        assertEquals(3.0, player.getY(), DELTA);
        assertEquals(Direction.RIGHT, player.getCurrentDirection());
    }

    @Test
    void testLoseLife() {
        assertEquals(3, player.getLives());
        player.loseLife();
        assertEquals(2, player.getLives());
        assertTrue(player.isAlive());

        player.loseLife();
        assertEquals(1, player.getLives());
        assertTrue(player.isAlive());

        player.loseLife();
        assertEquals(0, player.getLives());
        assertFalse(player.isAlive());
    }

    @Test
    void testDie() {
        assertTrue(player.isAlive());
        player.die();
        assertFalse(player.isAlive());
    }

    @Test
    void testReset() {
        // Change player state first
        player.move(Direction.UP, 0.5);
        player.loseLife();
        player.incrementBombsPlaced();

        // Then reset
        player.reset(5.0, 6.0);

        assertEquals(5.0, player.getX(), DELTA);
        assertEquals(6.0, player.getY(), DELTA);
        assertEquals(2, player.getLives()); // Lives should not be reset by default
        assertTrue(player.isAlive());
        assertEquals(Direction.DOWN, player.getCurrentDirection());
        assertEquals(0, player.getBombsPlaced());
    }

    @Test
    void testResetWithLivesReset() {
        player.loseLife();
        assertEquals(2, player.getLives());

        player.reset(5.0, 6.0, true); // Reset with lives reset

        assertEquals(3, player.getLives()); // Lives should be reset to default
    }

    @Test
    void testBombPlacement() {
        assertEquals(0, player.getBombsPlaced());
        player.incrementBombsPlaced();
        assertEquals(1, player.getBombsPlaced());

        player.decrementBombsPlaced();
        assertEquals(0, player.getBombsPlaced());

        // Test that we can't go below 0
        player.decrementBombsPlaced();
        assertEquals(0, player.getBombsPlaced());
    }

    @Test
    void testPowerUps() {
        // Test starting values
        assertEquals(1, player.getMaxBombs());
        assertEquals(1, player.getFirePower());
        double initialSpeed = player.getSpeed();

        // Test bomb capacity increase
        player.increaseBombCapacity();
        assertEquals(2, player.getMaxBombs());

        // Test fire power increase
        player.increaseFirePower();
        assertEquals(2, player.getFirePower());

        // Test speed increase
        player.increaseSpeed();
        assertEquals(initialSpeed + 0.5, player.getSpeed(), DELTA);

        // Test life addition
        assertEquals(3, player.getLives());
        player.addLife();
        assertEquals(4, player.getLives());
    }

    @Test
    void testApplyPowerUp() {
        // Test BOMB_UP
        player.applyPowerUp(PowerUp.Type.BOMB_UP);
        assertEquals(2, player.getMaxBombs());

        // Test FIRE_UP
        player.applyPowerUp(PowerUp.Type.FIRE_UP);
        assertEquals(2, player.getFirePower());

        // Test SPEED_UP
        double initialSpeed = player.getSpeed();
        player.applyPowerUp(PowerUp.Type.SPEED_UP);
        assertEquals(initialSpeed + 0.5, player.getSpeed(), DELTA);

        // Test EXTRA_LIFE
        player.applyPowerUp(PowerUp.Type.EXTRA_LIFE);
        assertEquals(4, player.getLives());
    }

    @Test
    void testSetDirection() {
        assertEquals(Direction.DOWN, player.getCurrentDirection());

        player.setDirection(Direction.UP);
        assertEquals(Direction.UP, player.getCurrentDirection());

        // Setting direction to NONE shouldn't change lastValidDirection
        player.setDirection(Direction.NONE);
        assertEquals(Direction.NONE, player.getCurrentDirection());
    }
}
