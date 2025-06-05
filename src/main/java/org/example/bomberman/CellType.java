package org.example.bomberman;

public enum CellType {
    EMPTY,           // Case vide
    WALL,            // Mur indestructible
    DESTRUCTIBLE_WALL, // Mur destructible
    BOMB,            // Case avec bombe
    EXPLOSION        // Case en explosion
}
