package fr.amu.iut.bomberman.model;

import fr.amu.iut.bomberman.utils.Direction;
import javafx.application.Platform;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;

/**
 * Intelligence artificielle équilibrée pour un joueur bot
 * Ce bot place des bombes pour casser des blocs et évite les dangers de manière intelligente
 *
 * @author Super Bomberman Team
 * @version 2.1
 */
public class BotPlayer {
    private final PlayerProfile botProfile;
    private final GameBoard gameBoard;
    private final Player botControlledPlayer;
    private final Random random = new Random();
    private boolean isActive = false;
    private Thread botThread;

    // Délais pour les actions du bot (en ms)
    private static final int DECISION_DELAY = 800; // Plus lent pour éviter les va-et-vient
    private static final int BOMB_COOLDOWN = 1500; // Réduit pour placer plus de bombes
    private long lastBombTime = 0;
    private Direction currentDirection = Direction.NONE;

    // Variables pour éviter les bombes placées par le bot
    private List<BombPosition> botPlacedBombs = new ArrayList<>();
    private static final int BOMB_AVOIDANCE_DURATION = 3500; // 3.5 secondes d'évitement

    // Classe interne pour stocker les positions des bombes du bot
    private static class BombPosition {
        int x, y;
        long placementTime;

        BombPosition(int x, int y, long time) {
            this.x = x;
            this.y = y;
            this.placementTime = time;
        }

        boolean isExpired(long currentTime) {
            return currentTime - placementTime > BOMB_AVOIDANCE_DURATION;
        }
    }

    /**
     * Crée un nouvel assistant de bot
     */
    public BotPlayer(Player player, GameBoard gameBoard) {
        this.botProfile = new PlayerProfile("Bot", "Bomberman", "BOT");
        this.botControlledPlayer = player;
        this.gameBoard = gameBoard;
        System.out.println("Bot créé avec stratégie équilibrée");
    }

    /**
     * Démarre le thread du bot
     */
    public void activate() {
        if (isActive) return;

        isActive = true;
        System.out.println("Bot activé - Joueur contrôlé: " + botControlledPlayer.getName());

        botThread = new Thread(this::runBotLogic);
        botThread.setDaemon(true);
        botThread.setName("Bot-Thread");
        botThread.start();

        Platform.runLater(() -> {
            System.out.println("Bot démarré");
            makeDecision();
        });
    }

    /**
     * Arrête le thread du bot
     */
    public void deactivate() {
        if (!isActive) return;
        System.out.println("Désactivation du bot...");
        isActive = false;
        if (botThread != null) {
            botThread.interrupt();
        }
    }

    /**
     * Logique principale du bot
     */
    private void runBotLogic() {
        while (isActive && !Thread.currentThread().isInterrupted()) {
            try {
                if (botControlledPlayer.isAlive()) {
                    Platform.runLater(this::makeDecision);
                }
                Thread.sleep(DECISION_DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Prend UNE SEULE décision par appel
     */
    private void makeDecision() {
        if (botControlledPlayer == null || !botControlledPlayer.isAlive()) {
            return;
        }

        // Nettoyer les anciennes bombes expirées
        cleanExpiredBombs();

        int botX = (int) Math.floor(botControlledPlayer.getX());
        int botY = (int) Math.floor(botControlledPlayer.getY());

        // PRIORITÉ 1: Fuir si on est exactement sur une bombe ou trop proche
        if (isInImmediateDanger(botX, botY)) {
            System.out.println("BOT: DANGER IMMÉDIAT - Fuite !");
            escapeFromDanger();
            return; // Une seule action par tour
        }

        // PRIORITÉ 2: Placer une bombe si c'est stratégique ET qu'on peut s'échapper
        if (shouldPlaceBombHere(botX, botY)) {
            System.out.println("BOT: Placement de bombe stratégique");
            placeBombAndEscape();
            return; // Une seule action par tour
        }

        // PRIORITÉ 3: Se déplacer vers un objectif
        System.out.println("BOT: Recherche d'objectif");
        moveTowardsBreakableWall();
    }

    /**
     * Vérifie si le bot est en danger immédiat (sur une bombe ou très proche)
     */
    private boolean isInImmediateDanger(int botX, int botY) {
        // Vérifier si on est exactement sur une bombe
        if (gameBoard.getTile(botX, botY) == GameBoard.TileType.BOMB) {
            return true;
        }

        // Vérifier les bombes adjacentes (rayon de 1 case seulement pour l'urgence)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int checkX = botX + dx;
                int checkY = botY + dy;

                if (checkX >= 0 && checkX < GameBoard.GRID_WIDTH &&
                        checkY >= 0 && checkY < GameBoard.GRID_HEIGHT) {
                    if (gameBoard.getTile(checkX, checkY) == GameBoard.TileType.BOMB) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * S'échappe du danger en trouvant la première direction sûre
     */
    private void escapeFromDanger() {
        int botX = (int) Math.floor(botControlledPlayer.getX());
        int botY = (int) Math.floor(botControlledPlayer.getY());

        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        shuffleArray(directions); // Randomiser pour éviter les patterns

        for (Direction dir : directions) {
            int newX = botX;
            int newY = botY;

            switch (dir) {
                case UP -> newY--;
                case DOWN -> newY++;
                case LEFT -> newX--;
                case RIGHT -> newX++;
            }

            if (isSafePosition(newX, newY)) {
                moveToPosition(newX, newY);
                System.out.println("BOT: Fuite réussie vers " + dir);
                return;
            }
        }

        System.out.println("BOT: Aucune échappatoire trouvée !");
    }

    /**
     * Vérifie si le bot devrait placer une bombe à sa position actuelle
     */
    private boolean shouldPlaceBombHere(int x, int y) {
        // Vérifier le cooldown et la capacité
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBombTime < BOMB_COOLDOWN || !botControlledPlayer.canPlaceBomb()) {
            return false;
        }

        // Ne pas placer si il y a déjà une bombe ici
        if (gameBoard.getTile(x, y) == GameBoard.TileType.BOMB) {
            return false;
        }

        // Vérifier s'il y a des murs cassables dans le rayon d'action
        int firepower = botControlledPlayer.getFirePower();
        boolean hasTarget = false;

        // Vérifier les 4 directions
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        for (Direction dir : directions) {
            for (int i = 1; i <= firepower; i++) {
                int checkX = x;
                int checkY = y;

                switch (dir) {
                    case UP -> checkY = y - i;
                    case DOWN -> checkY = y + i;
                    case LEFT -> checkX = x - i;
                    case RIGHT -> checkX = x + i;
                }

                if (checkX < 0 || checkX >= GameBoard.GRID_WIDTH ||
                        checkY < 0 || checkY >= GameBoard.GRID_HEIGHT) {
                    break;
                }

                GameBoard.TileType tile = gameBoard.getTile(checkX, checkY);
                if (tile == GameBoard.TileType.BREAKABLE_WALL) {
                    hasTarget = true;
                    break;
                }
                if (tile == GameBoard.TileType.WALL) {
                    break; // Les murs solides arrêtent l'explosion
                }
            }
            if (hasTarget) break;
        }

        // Placer la bombe seulement s'il y a une cible ET qu'on peut s'échapper
        return hasTarget && canEscapeFromPosition(x, y);
    }

    /**
     * Vérifie si le bot peut s'échapper de sa position actuelle
     */
    private boolean canEscapeFromPosition(int bombX, int bombY) {
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};

        for (Direction dir : directions) {
            int escapeX = bombX;
            int escapeY = bombY;

            switch (dir) {
                case UP -> escapeY--;
                case DOWN -> escapeY++;
                case LEFT -> escapeX--;
                case RIGHT -> escapeX++;
            }

            if (isSafePosition(escapeX, escapeY)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Place une bombe et s'échappe immédiatement
     */
    private void placeBombAndEscape() {
        int gridX = (int) Math.floor(botControlledPlayer.getX());
        int gridY = (int) Math.floor(botControlledPlayer.getY());

        // Placer la bombe
        Bomb bomb = new Bomb(gridX, gridY, botControlledPlayer.getFirePower(), botControlledPlayer.getPlayerId());
        gameBoard.addBomb(bomb);
        botControlledPlayer.incrementBombsPlaced();

        // Enregistrer cette bombe pour l'éviter
        botPlacedBombs.add(new BombPosition(gridX, gridY, System.currentTimeMillis()));

        System.out.println("BOT: BOMBE placée à (" + gridX + ", " + gridY + ")");
        lastBombTime = System.currentTimeMillis();

        // S'échapper dans la direction la plus sûre
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        shuffleArray(directions);

        for (Direction dir : directions) {
            int escapeX = gridX;
            int escapeY = gridY;

            switch (dir) {
                case UP -> escapeY--;
                case DOWN -> escapeY++;
                case LEFT -> escapeX--;
                case RIGHT -> escapeX++;
            }

            if (isSafePosition(escapeX, escapeY)) {
                moveToPosition(escapeX, escapeY);
                System.out.println("BOT: Évasion vers " + dir);
                return;
            }
        }
    }

    /**
     * Se déplace vers le mur cassable le plus proche
     */
    private void moveTowardsBreakableWall() {
        int botX = (int) Math.floor(botControlledPlayer.getX());
        int botY = (int) Math.floor(botControlledPlayer.getY());

        // Trouver le mur cassable le plus proche
        int closestX = -1, closestY = -1;
        int minDistance = Integer.MAX_VALUE;

        for (int x = 0; x < GameBoard.GRID_WIDTH; x++) {
            for (int y = 0; y < GameBoard.GRID_HEIGHT; y++) {
                if (gameBoard.getTile(x, y) == GameBoard.TileType.BREAKABLE_WALL) {
                    int distance = Math.abs(x - botX) + Math.abs(y - botY);
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestX = x;
                        closestY = y;
                    }
                }
            }
        }

        if (closestX != -1) {
            // Se déplacer vers le mur cassable
            Direction bestDir = getBestDirectionTo(botX, botY, closestX, closestY);
            if (bestDir != Direction.NONE) {
                int newX = botX, newY = botY;
                switch (bestDir) {
                    case UP -> newY--;
                    case DOWN -> newY++;
                    case LEFT -> newX--;
                    case RIGHT -> newX++;
                }

                if (isSafePosition(newX, newY)) {
                    moveToPosition(newX, newY);
                    System.out.println("BOT: Avance vers mur cassable (" + closestX + "," + closestY + ") - " + bestDir);
                    return;
                }
            }
        }

        // Si pas de mur ou chemin bloqué, mouvement aléatoire
        moveRandomlySafe();
    }

    /**
     * Trouve la meilleure direction pour aller vers une cible
     */
    private Direction getBestDirectionTo(int fromX, int fromY, int toX, int toY) {
        int dx = toX - fromX;
        int dy = toY - fromY;

        // Prioriser la direction avec la plus grande distance
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            return dy > 0 ? Direction.DOWN : Direction.UP;
        }
    }

    /**
     * Mouvement aléatoire sécurisé
     */
    private void moveRandomlySafe() {
        int botX = (int) Math.floor(botControlledPlayer.getX());
        int botY = (int) Math.floor(botControlledPlayer.getY());

        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        shuffleArray(directions);

        for (Direction dir : directions) {
            int newX = botX, newY = botY;
            switch (dir) {
                case UP -> newY--;
                case DOWN -> newY++;
                case LEFT -> newX--;
                case RIGHT -> newX++;
            }

            if (isSafePosition(newX, newY)) {
                moveToPosition(newX, newY);
                System.out.println("BOT: Mouvement aléatoire sécurisé - " + dir);
                return;
            }
        }

        System.out.println("BOT: Aucun mouvement possible");
    }

    /**
     * Vérifie si une position est sûre (pas de bombe, pas d'obstacle)
     */
    private boolean isSafePosition(int x, int y) {
        // Vérifier les limites
        if (x < 0 || x >= GameBoard.GRID_WIDTH || y < 0 || y >= GameBoard.GRID_HEIGHT) {
            return false;
        }

        // Vérifier les obstacles physiques
        GameBoard.TileType tile = gameBoard.getTile(x, y);
        if (tile == GameBoard.TileType.WALL ||
                tile == GameBoard.TileType.BREAKABLE_WALL ||
                tile == GameBoard.TileType.BOMB) {
            return false;
        }

        // Éviter les bombes placées par le bot récemment
        for (BombPosition bomb : botPlacedBombs) {
            if (bomb.x == x && bomb.y == y) {
                return false;
            }
        }

        return true;
    }

    /**
     * Déplace le bot à une position spécifique
     */
    private void moveToPosition(int gridX, int gridY) {
        double targetX = gridX + 0.5;
        double targetY = gridY + 0.5;
        botControlledPlayer.setPosition(targetX, targetY);
    }

    /**
     * Nettoie les bombes expirées de la liste
     */
    private void cleanExpiredBombs() {
        long currentTime = System.currentTimeMillis();
        botPlacedBombs.removeIf(bomb -> bomb.isExpired(currentTime));
    }

    /**
     * Mélange un tableau (algorithme de Fisher-Yates)
     */
    private void shuffleArray(Direction[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            Direction temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

    /**
     * Retourne le profil du bot
     */
    public PlayerProfile getProfile() {
        return botProfile;
    }
}