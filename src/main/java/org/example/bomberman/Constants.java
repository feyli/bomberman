package org.example.bomberman;

public class Constants {
    // Dimensions du jeu
    public static final int BOARD_WIDTH = 15;
    public static final int BOARD_HEIGHT = 13;
    public static final int CELL_SIZE = 40;

    // Timing
    public static final int BOMB_TIMER = 3000; // 3 secondes en ms
    public static final int EXPLOSION_DURATION = 1000; // 1 seconde
    public static final int GAME_LOOP_DELAY = 16; // ~60 FPS

    // Gameplay
    public static final int DEFAULT_BOMB_COUNT = 1;
    public static final int DEFAULT_BOMB_RANGE = 1;
    public static final int DEFAULT_PLAYER_SPEED = 1;
    public static final int DEFAULT_LIVES = 3;
    public static final int MAX_PLAYERS = 4;

    // Probabilités
    public static final double DESTRUCTIBLE_WALL_PROBABILITY = 0.7;
    public static final double POWERUP_DROP_PROBABILITY = 0.3;
}
