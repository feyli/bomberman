package org.example.bomberman;

import java.util.Objects;

public class Position {
    private final int x; // Coordonnée X sur la grille
    private final int y; // Coordonnée Y sur la grille

    // Constructeur
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position move(Direction direction) {
        switch (direction) {
            case UP:
                return new Position(x, y - 1); // Monte d'une unité
            case DOWN:
                return new Position(x, y + 1); // Descend d'une unité
            case LEFT:
                return new Position(x - 1, y); // Va à gauche d'une unité
            case RIGHT:
                return new Position(x + 1, y); // Va à droite d'une unité
            default:
                // Si pour une raison quelconque la direction n'est pas reconnue
                return this; // Pas de mouvement
        }
    }

    // Getters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // Déplace la position en fournissant directement un changement (dx, dy)
    public Position move(int dx, int dy) {
        return new Position(x + dx, y + dy);
    }

    // Vérifie si une position est adjacente à une autre
    public boolean isAdjacent(Position other) {
        int dx = Math.abs(this.x - other.x);
        int dy = Math.abs(this.y - other.y);
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1); // Une seule unité d'écart
    }

    // Vérifie si deux positions sont identiques
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    // Génère un hash code (utilisé pour comparer dans des collections)
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    // Retourne une chaîne lisible pour une position (par exemple : "Position{x=1, y=2}")
    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}