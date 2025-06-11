package fr.amu.iut.bomberman.model;

import fr.amu.iut.bomberman.utils.Direction;
import javafx.application.Platform;

import java.util.List;
import java.util.Random;

// Constantes pour le déplacement du bot


/**
 * Intelligence artificielle simple pour un joueur bot
 * Ce bot place des bombes pour casser des blocs et évite les dangers
 *
 * @author Super Bomberman Team
 * @version 1.0
 */
public class BotPlayer {
    private final PlayerProfile botProfile;
    private final GameBoard gameBoard;
    private final Player botControlledPlayer;
    private final Random random = new Random();
    private boolean isActive = false;
    private Thread botThread;

    // Délais pour les actions du bot (en ms)
    private static final int DECISION_DELAY = 600; // délai ralenti pour que le bot ne se déplace pas trop vite
    private static final int BOMB_COOLDOWN = 2000; // délai entre les bombs
    private long lastBombTime = 0;
    private static final double BOT_DELTA_TIME = 0.05; // Ajusté pour un mouvement comparable aux joueurs
    private Direction currentDirection = Direction.NONE; // Direction actuelle du bot

    /**
     * Crée un nouvel assistant de bot
     *
     * @param player Le joueur contrôlé par le bot
     * @param gameBoard Le plateau de jeu
     */
    public BotPlayer(Player player, GameBoard gameBoard) {
        this.botProfile = new PlayerProfile("Bot", "Bomberman", "BOT");
        this.botControlledPlayer = player;
        this.gameBoard = gameBoard;

        // S'assurer que le bot n'est pas invincible en permanence
        // L'invincibilité temporaire initiale est gérée par la classe Player, mais
        // nous nous assurons qu'elle n'est pas maintenue indéfiniment pour le bot
        System.out.println("Bot créé - vérifier que l'invincibilité n'est pas permanente");
    }

    /**
     * Détermine la meilleure direction pour s'éloigner d'une position donnée
     */
    private Direction getBestDirectionAwayFromPosition(int x, int y) {
        double botX = botControlledPlayer.getX();
        double botY = botControlledPlayer.getY();

        // Calculer les vecteurs directionnels (s'éloigner de la position x,y)
        double dx = botX - x;
        double dy = botY - y;

        // Déterminer les meilleures directions en fonction des vecteurs
        Direction horizontalDirection = (dx > 0) ? Direction.RIGHT : Direction.LEFT;
        Direction verticalDirection = (dy > 0) ? Direction.DOWN : Direction.UP;

        // Priorité à la direction qui a la plus grande distance à parcourir
        Direction preferredDirection = (Math.abs(dx) > Math.abs(dy)) ? horizontalDirection : verticalDirection;
        Direction alternateDirection = (Math.abs(dx) > Math.abs(dy)) ? verticalDirection : horizontalDirection;

        // Essayer d'abord la direction préférée
        if (canMoveInDirection(preferredDirection)) {
            return preferredDirection;
        }

        // Sinon essayer la direction alternative
        if (canMoveInDirection(alternateDirection)) {
            return alternateDirection;
        }

        // Si aucune des directions principales ne fonctionne, essayer toutes les autres
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        for (Direction dir : directions) {
            if (canMoveInDirection(dir)) {
                return dir;
            }
        }

        // Aucune direction possible
        return Direction.NONE;
    }

    /**
     * Vérifie si le bot peut se déplacer dans une direction donnée
     */
    private boolean canMoveInDirection(Direction direction) {
        double currentX = botControlledPlayer.getX();
        double currentY = botControlledPlayer.getY();
        double moveDistance = botControlledPlayer.getSpeed() * BOT_DELTA_TIME;

        // Calculer la nouvelle position
        double newX = currentX;
        double newY = currentY;

        switch (direction) {
            case UP -> newY -= moveDistance;
            case DOWN -> newY += moveDistance;
            case LEFT -> newX -= moveDistance;
            case RIGHT -> newX += moveDistance;
            case NONE -> { return false; }
        }

        return isValidPosition(newX, newY);
    }

    /**
     * Vérifie si une position est valide (pas de collision avec les murs ou les bombes)
     */
    private boolean isValidPosition(double x, double y) {
        // Vérifier les limites du plateau
        if (x < 0 || x >= GameBoard.GRID_WIDTH || y < 0 || y >= GameBoard.GRID_HEIGHT) {
            return false;
        }

        // Convertir en coordonnées de grille
        int gridX = (int) Math.floor(x);
        int gridY = (int) Math.floor(y);

        // Vérifier si la case est un mur, un mur cassable ou une bombe
        GameBoard.TileType tile = gameBoard.getTile(gridX, gridY);
        return tile != GameBoard.TileType.WALL &&
                tile != GameBoard.TileType.BREAKABLE_WALL &&
                tile != GameBoard.TileType.BOMB;  // Empêche de traverser les bombes
    }

    /**
     * Mélange un tableau d'éléments (algorithme de Fisher-Yates)
     */
    private void shuffleArray(Direction[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            // Échanger les éléments
            Direction temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

    /**
     * Démarre le thread du bot
     */
    public void activate() {
        if (isActive) return;

        isActive = true;
        System.out.println("Bot activé - Joueur contrôlé: " + botControlledPlayer.getName());
        System.out.println("Position initiale du bot: (" + botControlledPlayer.getX() + ", " + botControlledPlayer.getY() + ")");

        botThread = new Thread(this::runBotLogic);
        botThread.setDaemon(true);
        botThread.setName("Bot-Thread");
        botThread.start();

        System.out.println("Thread du bot démarré: " + botThread.getName() + " (daemon: " + botThread.isDaemon() + ")");

        // Première décision immédiate pour tester
        Platform.runLater(() -> {
            System.out.println("Test initial du bot - Déplacement aléatoire");
            moveRandomly();
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
            System.out.println("Thread du bot interrompu");
        }
    }

    /**
     * Logique principale du bot
     */
    private void runBotLogic() {
        while (isActive && !Thread.currentThread().isInterrupted()) {
            try {
                // Prendre une décision seulement si le joueur est vivant
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
     * Prend une décision sur l'action à effectuer
     */
    private void makeDecision() {
        // Vérifier si le joueur existe et est en vie
        if (botControlledPlayer == null || !botControlledPlayer.isAlive()) {
            System.out.println("Bot ne peut pas prendre de décision: joueur null ou mort");
            return;
        }

        // PREMIÈRE PRIORITÉ: Essayer de placer une bombe
        if (forceBombPlacement()) {
            return; // Si une bombe a été placée, s'arrêter là
        }

        // Si on ne peut pas placer de bombe (cooldown), se déplacer
        moveRandomly();
    }

    /**
     * Force le bot à placer une bombe si possible
     * @return true si une bombe a été placée, false sinon
     */
    private boolean forceBombPlacement() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBombTime > BOMB_COOLDOWN && botControlledPlayer.canPlaceBomb()) {
            // Au lieu d'appeler botControlledPlayer.placeBomb(), on utilise notre méthode spéciale
            forcePlaceBombOnBoard();
            return true;
        }
        return false;
    }

    /**
     * Force la création d'une bombe directement sur le plateau de jeu
     */
    private void forcePlaceBombOnBoard() {
        int gridX = (int) Math.floor(botControlledPlayer.getX());
        int gridY = (int) Math.floor(botControlledPlayer.getY());

        System.out.println("BOT: Tentative de placement de bombe à la position (" + gridX + ", " + gridY + ")");

        // Vérifie si la position est libre (pas déjà une bombe)
        if (gameBoard.getTile(gridX, gridY) != GameBoard.TileType.BOMB) {
            // Crée une nouvelle bombe et la place sur le plateau
            // Passe l'ID du joueur au lieu de l'objet Player
            Bomb bomb = new Bomb(gridX, gridY, botControlledPlayer.getFirePower(), botControlledPlayer.getPlayerId());
            gameBoard.addBomb(bomb);
            botControlledPlayer.incrementBombsPlaced();

            System.out.println("BOT: BOMBE PLACÉE avec succès à (" + gridX + ", " + gridY + ")");
            lastBombTime = System.currentTimeMillis();

            // S'éloigner de la bombe immédiatement
            moveAwayFromBomb();
        } else {
            System.out.println("BOT: Impossible de placer une bombe - position déjà occupée!");
            moveRandomly(); // Se déplacer ailleurs
        }
    }

    /**
     * Déplace le bot aléatoirement d'une case dans la matrice
     */
    private void moveRandomly() {
        // Mélanger les directions pour essayer dans un ordre aléatoire
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        shuffleArray(directions);

        // Essayer chaque direction jusqu'à en trouver une valide
        for (Direction direction : directions) {
            if (moveOneGridCell(direction)) {
                System.out.println("Bot se déplace aléatoirement - Direction: " + direction);
                return; // Sortir après un seul déplacement
            }
        }

        System.out.println("Bot ne peut pas se déplacer - toutes les directions sont bloquées");
    }

    /**
     * S'éloigne de la bombe que le bot vient de placer
     */
    private void moveAwayFromBomb() {
        // Position actuelle du bot dans la grille
        int currentGridX = (int) Math.floor(botControlledPlayer.getX());
        int currentGridY = (int) Math.floor(botControlledPlayer.getY());

        // Trouver la meilleure direction pour s'éloigner
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};

        // Essayer les directions dans un ordre qui maximise l'éloignement
        for (Direction direction : directions) {
            int newGridX = currentGridX;
            int newGridY = currentGridY;

            switch (direction) {
                case UP -> newGridY = currentGridY - 1;
                case DOWN -> newGridY = currentGridY + 1;
                case LEFT -> newGridX = currentGridX - 1;
                case RIGHT -> newGridX = currentGridX + 1;
                default -> {}
            }

            if (isValidGridPosition(newGridX, newGridY)) {
                teleportToGridPosition(newGridX, newGridY);
                System.out.println("Bot s'éloigne de la bombe - Direction: " + direction);
                return;
            }
        }

        System.out.println("Bot ne peut pas s'éloigner de la bombe - toutes les directions sont bloquées");
    }

    /**
     * Déplace le bot vers un bloc cassable
     */
    private void moveTowardsBreakableBlock() {
        // Ici, nous simplifions et nous déplaçons simplement dans une direction aléatoire
        // Une implémentation plus avancée rechercherait le chemin vers le bloc cassable le plus proche
        moveRandomly();
    }

    /**
     * Fait éviter les dangers au bot
     */
    private void avoidDanger() {
        // Implémentation simplifiée - se déplacer aléatoirement pour l'instant
        // Une implémentation plus avancée détecterait les bombes et s'en éloignerait
        moveRandomly();
    }

    /**
     * Choisit une direction aléatoire dans laquelle le bot peut se déplacer
     */
    private void chooseRandomDirection() {
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        shuffleArray(directions);

        for (Direction dir : directions) {
            if (canMoveInDirection(dir)) {
                currentDirection = dir;
                System.out.println("Bot choisit une nouvelle direction aléatoire: " + dir);
                return;
            }
        }

        // Si aucune direction n'est valide, rester immobile
        currentDirection = Direction.NONE;
    }

    /**
     * Téléporte le bot directement à une position de la grille
     * @param gridX Coordonnée X dans la grille
     * @param gridY Coordonnée Y dans la grille
     */
    private void teleportToGridPosition(int gridX, int gridY) {
        // Obtenir la position actuelle
        double currentX = botControlledPlayer.getX();
        double currentY = botControlledPlayer.getY();

        // Calculer la position cible (centre de la case)
        double targetX = gridX + 0.5;
        double targetY = gridY + 0.5;

        // Simuler un mouvement progressif pour déclencher les collisions
        // en faisant plusieurs petits pas entre la position actuelle et la cible
        double steps = 5;  // Nombre de pas intermédiaires

        for (int i = 1; i <= steps; i++) {
            double ratio = i / steps;
            double intermediateX = currentX + (targetX - currentX) * ratio;
            double intermediateY = currentY + (targetY - currentY) * ratio;

            // Mettre à jour la position du joueur
            botControlledPlayer.setPosition(intermediateX, intermediateY);

            // Petite pause pour que le moteur de jeu ait le temps de traiter les collisions
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // S'assurer que la position finale est exactement celle demandée
        botControlledPlayer.setPosition(targetX, targetY);
        System.out.println("Bot déplacé à la case (" + gridX + ", " + gridY + ")");
    }

    /**
     * Déplace le bot d'une case dans la direction donnée (par téléportation)
     * @param direction La direction dans laquelle se déplacer
     * @return true si le déplacement a réussi, false sinon
     */
    private boolean moveOneGridCell(Direction direction) {
        // Position actuelle du bot dans la grille
        int currentGridX = (int) Math.floor(botControlledPlayer.getX());
        int currentGridY = (int) Math.floor(botControlledPlayer.getY());

        // Calcule la nouvelle position dans la grille
        int newGridX = currentGridX;
        int newGridY = currentGridY;

        switch (direction) {
            case UP -> newGridY = currentGridY - 1;
            case DOWN -> newGridY = currentGridY + 1;
            case LEFT -> newGridX = currentGridX - 1;
            case RIGHT -> newGridX = currentGridX + 1;
            case NONE -> { return false; }
        }

        // Vérifie si la nouvelle position est valide
        if (isValidGridPosition(newGridX, newGridY)) {
            teleportToGridPosition(newGridX, newGridY);
            return true;
        }

        return false;
    }

    /**
     * Vérifie si une position de la grille est valide (pas de mur ni de bombe)
     * @param gridX Coordonnée X dans la grille
     * @param gridY Coordonnée Y dans la grille
     * @return true si la position est valide, false sinon
     */
    private boolean isValidGridPosition(int gridX, int gridY) {
        // Vérifier les limites du plateau
        if (gridX < 0 || gridX >= GameBoard.GRID_WIDTH || gridY < 0 || gridY >= GameBoard.GRID_HEIGHT) {
            return false;
        }

        // Vérifier si la case est un mur, un mur cassable ou une bombe
        GameBoard.TileType tile = gameBoard.getTile(gridX, gridY);
        return tile != GameBoard.TileType.WALL &&
                tile != GameBoard.TileType.BREAKABLE_WALL &&
                tile != GameBoard.TileType.BOMB; // Empêche de traverser les bombes
    }

    /**
     * Vérifie s'il y a un mur cassable à proximité du bot
     * @param gridX Coordonnée X dans la grille
     * @param gridY Coordonnée Y dans la grille
     * @return true si un mur cassable est adjacent à la position donnée
     */
    private boolean isBreakableWallNearby(int gridX, int gridY) {
        // Vérifier les 4 directions adjacentes
        int[][] adjacentCells = {
                {gridX - 1, gridY}, // Gauche
                {gridX + 1, gridY}, // Droite
                {gridX, gridY - 1}, // Haut
                {gridX, gridY + 1}  // Bas
        };

        for (int[] cell : adjacentCells) {
            int x = cell[0];
            int y = cell[1];

            // Vérifier si les coordonnées sont dans les limites
            if (x >= 0 && x < GameBoard.GRID_WIDTH && y >= 0 && y < GameBoard.GRID_HEIGHT) {
                // Vérifier si la case contient un mur cassable
                if (gameBoard.getTile(x, y) == GameBoard.TileType.BREAKABLE_WALL) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Retourne le profil du bot
     *
     * @return Le profil du bot
     */
    public PlayerProfile getProfile() {
        return botProfile;
    }
}
