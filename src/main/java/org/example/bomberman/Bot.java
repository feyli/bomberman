package org.example.bomberman;

import java.util.List;

public class Bot {
    private Position position; // Position actuelle du bot
    private int id;

    public Bot(int id, Position startPosition) {
        this.id = id;
    }

    public Bot(Position startPosition) {
        this.position = startPosition;
    }

    public Position getPosition() {
        return position;
    }

    // Déplacement de base : échapper aux bombes ou se rapprocher d'une cible
    public void makeMove(Position playerPosition, List<Position> bombPositions) {
        // Vérifier si on doit éviter une bombe
        Position safeMove = findSafePosition(bombPositions);
        if (safeMove != null) {
            this.position = safeMove; // Échapper à une bombe
            return;
        }

        // Si pas de bombe à proximité, se rapprocher du joueur
        this.position = moveTowards(playerPosition);
    }

    public void performAction() {
        System.out.println("Bot " + id + " effectue une action.");
        // Ajoutez ici la logique spécifique au bot (déplacement, pose de bombe, etc.)
    }

    // Trouver une position sûre (échapper aux bombes)
    private Position findSafePosition(List<Position> bombPositions) {
        for (Position bomb : bombPositions) {
            if (this.position.isAdjacent(bomb)) {
                // Calculer une position éloignée de la bombe
                for (Position potentialMove : getPossibleMoves()) {
                    if (!potentialMove.isAdjacent(bomb)) {
                        return potentialMove; // Renvoie une position sûre
                    }
                }
            }
        }
        return null; // Aucune bombe dangereuse détectée
    }

    // Se rapprocher d'une cible (par exemple le joueur)
    private Position moveTowards(Position target) {
        int dx = target.getX() - position.getX();
        int dy = target.getY() - position.getY();

        // Déplacement en priorité sur l'axe le plus éloigné
        if (Math.abs(dx) > Math.abs(dy)) {
            return new Position(position.getX() + Integer.signum(dx), position.getY());
        } else {
            return new Position(position.getX(), position.getY() + Integer.signum(dy));
        }
    }

    // Retourne les positions accessibles depuis la position actuelle
    private List<Position> getPossibleMoves() {
        return List.of(
                position.move(1, 0),  // Droite
                position.move(-1, 0), // Gauche
                position.move(0, 1),  // Bas
                position.move(0, -1)  // Haut
        );
    }

    @Override
    public String toString() {
        return "Bot{" +
                "position=" + position +
                '}';
    }
}