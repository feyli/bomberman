package org.example.bomberman;

import java.util.Random;

public enum Direction {
    UP, DOWN, LEFT, RIGHT;

    // Retourne une direction aléatoire
    public static Direction getRandomDirection() {
        Direction[] values = Direction.values();
        Random random = new Random();
        return values[random.nextInt(values.length)];
    }
}