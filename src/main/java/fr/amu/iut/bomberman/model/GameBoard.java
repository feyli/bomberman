package fr.amu.iut.bomberman.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Classe représentant le plateau de jeu
 * Gère la grille, les murs, les bombes et les power-ups
 *
 * @author Super Bomberman Team
 * @version 1.0
 */
public class GameBoard {

    public static final int GRID_WIDTH = 15;
    public static final int GRID_HEIGHT = 13;
    public static final int TILE_SIZE = 48;

    // Types de tuiles
    public enum TileType {
        EMPTY,
        WALL,           // Mur indestructible
        BREAKABLE_WALL, // Mur destructible
        BOMB,
        EXPLOSION,
        POWER_UP
    }

    private TileType[][] grid;
    private List<Bomb> bombs;
    private List<PowerUp> powerUps;
    private List<Explosion> explosions;
    private Random random;

    // Observable pour les changements
    private final ObjectProperty<TileType[][]> gridProperty;

    /**
     * Constructeur du plateau de jeu
     */
    public GameBoard() {
        this.grid = new TileType[GRID_WIDTH][GRID_HEIGHT];
        this.bombs = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.explosions = new ArrayList<>();
        this.random = new Random();
        this.gridProperty = new SimpleObjectProperty<>(grid);

        initializeBoard();
    }

    /**
     * Initialise le plateau avec le pattern classique de Bomberman
     */
    private void initializeBoard() {
        // Remplir avec des cases vides
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                grid[x][y] = TileType.EMPTY;
            }
        }

        // Ajouter les murs indestructibles (pattern en damier)
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                // Bordures
                if (x == 0 || x == GRID_WIDTH - 1 || y == 0 || y == GRID_HEIGHT - 1) {
                    grid[x][y] = TileType.WALL;
                }
                // Pattern damier
                else if (x % 2 == 0 && y % 2 == 0) {
                    grid[x][y] = TileType.WALL;
                }
            }
        }

        // Ajouter des murs destructibles aléatoirement
        generateBreakableWalls();

        // S'assurer que les zones de départ sont libres
        clearStartingAreas();

        // Debug: afficher les zones de spawn
        System.out.println("=== PLATEAU INITIALISÉ ===");
        System.out.println("Zone Joueur 1: cases (1,1), (2,1), (1,2) libres");
        System.out.println("Zone Joueur 2: cases (13,11), (12,11), (13,10) libres");
        printBoardDebug();

        updateGridProperty();
    }

    /**
     * Affiche le plateau en mode debug (pour vérifier les spawns)
     */
    private void printBoardDebug() {
        System.out.println("État du plateau (zones de spawn):");
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                // Marquer les zones de spawn
                if ((x == 1 && y == 1) || (x == 13 && y == 11)) {
                    System.out.print("S "); // Spawn
                } else {
                    switch (grid[x][y]) {
                        case EMPTY -> System.out.print(". ");
                        case WALL -> System.out.print("# ");
                        case BREAKABLE_WALL -> System.out.print("B ");
                        default -> System.out.print("? ");
                    }
                }
            }
            System.out.println();
        }
    }

    /**
     * Génère des murs destructibles sur le plateau
     */
    private void generateBreakableWalls() {
        int wallCount = (int) ((GRID_WIDTH * GRID_HEIGHT) * 0.3); // 30% de murs destructibles

        for (int i = 0; i < wallCount; i++) {
            int x, y;
            int attempts = 0;
            do {
                x = random.nextInt(GRID_WIDTH);
                y = random.nextInt(GRID_HEIGHT);
                attempts++;
                // Éviter une boucle infinie
                if (attempts > 1000) break;
            } while (grid[x][y] != TileType.EMPTY || isStartingArea(x, y));

            if (attempts <= 1000 && grid[x][y] == TileType.EMPTY) {
                grid[x][y] = TileType.BREAKABLE_WALL;
            }
        }
    }

    /**
     * Vérifie si une position est dans une zone de départ
     *
     * @param x Position X
     * @param y Position Y
     * @return true si c'est une zone de départ
     */
    private boolean isStartingArea(int x, int y) {
        // Zone joueur 1 (coin haut-gauche) - 3x3 autour de (1,1)
        if (x >= 1 && x <= 3 && y >= 1 && y <= 3) {
            return true;
        }

        // Zone joueur 2 (coin bas-droit) - 3x3 autour de (13,11)
        if (x >= 11 && x <= 13 && y >= 9 && y <= 11) {
            return true;
        }

        return false;
    }

    /**
     * Nettoie les zones de départ des joueurs
     */
    private void clearStartingAreas() {
        // Zone Joueur 1 - autour de (1,1)
        clearArea(1, 1, 2);

        // Zone Joueur 2 - autour de (13,11)
        clearArea(13, 11, 2);
    }

    /**
     * Nettoie une zone autour d'une position
     */
    private void clearArea(int centerX, int centerY, int radius) {
        for (int x = Math.max(1, centerX - radius); x <= Math.min(GRID_WIDTH - 2, centerX + radius); x++) {
            for (int y = Math.max(1, centerY - radius); y <= Math.min(GRID_HEIGHT - 2, centerY + radius); y++) {
                // Ne pas toucher aux murs indestructibles du pattern damier
                if (!(x % 2 == 0 && y % 2 == 0)) {
                    grid[x][y] = TileType.EMPTY;
                }
            }
        }
    }

    /**
     * Ajoute une bombe au plateau
     */
    public void addBomb(Bomb bomb) {
        bombs.add(bomb);
        System.out.println("Bombe ajoutée à (" + bomb.getX() + ", " + bomb.getY() + ")");
    }

    /**
     * Supprime une bombe du plateau
     */
    public void removeBomb(Bomb bomb) {
        bombs.remove(bomb);
    }

    /**
     * Fait exploser une bombe
     */
    public void explodeBomb(Bomb bomb) {
        int centerX = bomb.getX();
        int centerY = bomb.getY();
        int range = bomb.getFirePower();

        // Centre de l'explosion
        addExplosion(centerX, centerY, Explosion.Type.CENTER);

        // Détruire les murs destructibles au centre
        if (getTile(centerX, centerY) == TileType.BREAKABLE_WALL) {
            setTile(centerX, centerY, TileType.EMPTY);
            // Chance de laisser un power-up
            if (Math.random() < 0.3) {
                addRandomPowerUp(centerX, centerY);
            }
        }

        // Directions : HAUT, BAS, GAUCHE, DROITE
        int[][] directions = {
                {0, -1}, // HAUT
                {0, 1},  // BAS
                {-1, 0}, // GAUCHE
                {1, 0}   // DROITE
        };

        Explosion.Type[] directionTypes = {
                Explosion.Type.VERTICAL,   // HAUT
                Explosion.Type.VERTICAL,   // BAS
                Explosion.Type.HORIZONTAL, // GAUCHE
                Explosion.Type.HORIZONTAL  // DROITE
        };

        Explosion.Type[] endTypes = {
                Explosion.Type.END_UP,    // HAUT
                Explosion.Type.END_DOWN,  // BAS
                Explosion.Type.END_LEFT,  // GAUCHE
                Explosion.Type.END_RIGHT  // DROITE
        };

        // Propager l'explosion dans chaque direction
        for (int dir = 0; dir < directions.length; dir++) {
            int dx = directions[dir][0];
            int dy = directions[dir][1];

            for (int i = 1; i <= range; i++) {
                int x = centerX + dx * i;
                int y = centerY + dy * i;

                // Vérifier les limites
                if (!isValidPosition(x, y)) {
                    break;
                }

                // Vérifier les obstacles
                TileType tile = getTile(x, y);
                if (tile == TileType.WALL) {
                    break; // Mur solide arrête l'explosion
                }

                // Ajouter l'explosion
                if (i == range || tile == TileType.BREAKABLE_WALL) {
                    // Fin de la branche d'explosion
                    addExplosion(x, y, endTypes[dir]);
                } else {
                    // Milieu de la branche
                    addExplosion(x, y, directionTypes[dir]);
                }

                // Détruire les murs destructibles
                if (tile == TileType.BREAKABLE_WALL) {
                    setTile(x, y, TileType.EMPTY);
                    // Chance de laisser un power-up
                    if (Math.random() < 0.3) {
                        addRandomPowerUp(x, y);
                    }
                    break; // Mur destructible arrête l'explosion
                }

                // Faire exploser les autres bombes (réaction en chaîne)
                Bomb bombAtPosition = getBombAt(x, y);
                if (bombAtPosition != null && bombAtPosition != bomb) {
                    bombAtPosition.forceExplode();
                }
            }
        }

        System.out.println("Explosion créée au centre (" + centerX + ", " + centerY + ") avec portée " + range);
    }

    /**
     * Obtient la bombe à une position donnée
     */
    private Bomb getBombAt(int x, int y) {
        for (Bomb bomb : bombs) {
            if (bomb.getX() == x && bomb.getY() == y) {
                return bomb;
            }
        }
        return null;
    }

    /**
     * Ajoute une explosion
     */
    private void addExplosion(int x, int y, Explosion.Type type) {
        explosions.add(new Explosion(x, y, type));
    }

    /**
     * Ajoute un power-up aléatoire à une position
     */
    private void addRandomPowerUp(int x, int y) {
        // Distribution des power-ups
        PowerUp.Type[] commonTypes = {
                PowerUp.Type.BOMB_UP,
                PowerUp.Type.FIRE_UP,
                PowerUp.Type.SPEED_UP
        };

        PowerUp.Type randomType;
        if (Math.random() < 0.1) { // 10% de chance pour une vie extra
            randomType = PowerUp.Type.EXTRA_LIFE;
        } else {
            randomType = commonTypes[random.nextInt(commonTypes.length)];
        }

        powerUps.add(new PowerUp(x, y, randomType));
        System.out.println("Power-up " + randomType + " ajouté à (" + x + ", " + y + ")");
    }

    /**
     * Ajoute un power-up spécifique
     */
    public void addPowerUp(PowerUp powerUp) {
        powerUps.add(powerUp);
    }

    /**
     * Supprime un power-up
     */
    public void removePowerUp(PowerUp powerUp) {
        powerUps.remove(powerUp);
    }

    /**
     * Vérifie s'il y a un power-up à une position donnée
     */
    public PowerUp getPowerUpAt(int x, int y) {
        for (PowerUp powerUp : powerUps) {
            if (powerUp.getX() == x && powerUp.getY() == y) {
                return powerUp;
            }
        }
        return null;
    }

    /**
     * Met à jour le plateau (explosions, bombes, etc.)
     *
     * @param deltaTime Temps écoulé
     * @return Liste des bombes qui ont explosé
     */
    public List<Bomb> update(double deltaTime) {
        List<Bomb> explodedBombs = new ArrayList<>();

        // Mise à jour des bombes
        Iterator<Bomb> bombIterator = bombs.iterator();
        while (bombIterator.hasNext()) {
            Bomb bomb = bombIterator.next();
            bomb.update(deltaTime);
            if (bomb.shouldExplode()) {
                explodedBombs.add(bomb);
                bombIterator.remove();
            }
        }

        // Mise à jour des explosions
        Iterator<Explosion> explosionIterator = explosions.iterator();
        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();
            explosion.update(deltaTime);
            if (explosion.isFinished()) {
                explosionIterator.remove();
            }
        }

        if (!explodedBombs.isEmpty() || !explosions.isEmpty()) {
            updateGridProperty();
        }

        return explodedBombs;
    }

    /**
     * Vérifie si une position est valide
     *
     * @param x Position X
     * @param y Position Y
     * @return true si valide
     */
    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT;
    }

    /**
     * Vérifie si une position est traversable
     *
     * @param x Position X
     * @param y Position Y
     * @return true si traversable
     */
    public boolean isWalkable(int x, int y) {
        if (!isValidPosition(x, y)) return false;
        TileType tile = grid[x][y];
        return tile == TileType.EMPTY || tile == TileType.POWER_UP;
    }

    /**
     * Vérifie si une position contient une bombe
     */
    public boolean hasBomb(int x, int y) {
        for (Bomb bomb : bombs) {
            if (bomb.getX() == x && bomb.getY() == y) {
                return true;
            }
        }
        return false;
    }

    /**
     * Collecte un power-up à une position
     *
     * @param x Position X
     * @param y Position Y
     * @return Le power-up collecté, ou null
     */
    public PowerUp collectPowerUp(int x, int y) {
        Iterator<PowerUp> iterator = powerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp powerUp = iterator.next();
            if (powerUp.getX() == x && powerUp.getY() == y) {
                iterator.remove();
                updateGridProperty();
                return powerUp;
            }
        }
        return null;
    }

    /**
     * Vérifie si une position contient une explosion
     *
     * @param x Position X
     * @param y Position Y
     * @return true si explosion
     */
    public boolean hasExplosion(int x, int y) {
        for (Explosion explosion : explosions) {
            if (explosion.getX() == x && explosion.getY() == y) {
                return true;
            }
        }
        return false;
    }

    /**
     * Réinitialise le plateau pour une nouvelle partie
     */
    public void reset() {
        bombs.clear();
        powerUps.clear();
        explosions.clear();

        // IMPORTANT: Régénérer complètement le plateau
        initializeBoard();

        System.out.println("Plateau réinitialisé pour un nouveau round");
    }

    /**
     * Met à jour la propriété observable de la grille
     */
    private void updateGridProperty() {
        gridProperty.set(grid);
    }

    /**
     * Définit le type d'une tuile
     */
    public void setTile(int x, int y, TileType type) {
        if (isValidPosition(x, y)) {
            grid[x][y] = type;
            updateGridProperty();
        }
    }

    // Getters
    public TileType[][] getGrid() {
        return grid;
    }

    public ObjectProperty<TileType[][]> gridProperty() {
        return gridProperty;
    }

    public List<Bomb> getBombs() {
        return new ArrayList<>(bombs);
    }

    public List<PowerUp> getPowerUps() {
        return new ArrayList<>(powerUps);
    }

    public List<Explosion> getExplosions() {
        return new ArrayList<>(explosions);
    }

    public TileType getTile(int x, int y) {
        if (isValidPosition(x, y)) {
            return grid[x][y];
        }
        return TileType.WALL;
    }
}