package fr.amu.iut.bomberman.model;

import fr.amu.iut.bomberman.utils.Direction;
import javafx.application.Platform;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;

/**
 * Intelligence artificielle équilibrée pour un joueur bot Bomberman
 *
 * FONCTIONNALITÉS PRINCIPALES :
 * - Détection intelligente des zones dangereuses (toutes les bombes + puissance variable)
 * - Placement stratégique de bombes avec vérification d'évasion
 * - Évasion multi-niveaux (proche puis étendue)
 * - Prévention des boucles de mouvement
 * - Recherche optimisée de murs cassables
 *
 * @author Super Bomberman Team
 * @version 2.4 Clean
 */
public class BotPlayer {
    // ================ CONFIGURATION ================
    private static final int DECISION_DELAY = 800;        // Délai entre chaque décision (ms)
    private static final int BOMB_COOLDOWN = 1500;        // Délai minimum entre bombes (ms)
    private static final int BOMB_AVOIDANCE_DURATION = 3500; // Durée d'évitement des bombes du bot (ms)
    private static final int MAX_RECENT_ACTIONS = 5;      // Nombre d'actions mémorisées pour éviter les boucles

    // ================ COMPOSANTS PRINCIPAUX ================
    private final PlayerProfile botProfile;               // Profil du bot
    private final GameBoard gameBoard;                    // Plateau de jeu
    private final Player botControlledPlayer;             // Joueur contrôlé par le bot
    private final Random random = new Random();           // Générateur de nombres aléatoires

    // ================ ÉTAT DU BOT ================
    private boolean isActive = false;                     // Statut d'activité du bot
    private Thread botThread;                             // Thread d'exécution du bot
    private long lastBombTime = 0;                        // Timestamp de la dernière bombe placée

    // ================ MÉMOIRE DU BOT ================
    private List<BombPosition> botPlacedBombs = new ArrayList<>();  // Bombes placées par le bot
    private List<String> recentActions = new ArrayList<>();         // Actions récentes pour éviter les boucles

    /**
     * Classe interne : Position d'une bombe avec sa puissance d'explosion
     * Permet le calcul précis des zones dangereuses
     */
    private static class BombPosition {
        int x, y;                    // Position de la bombe sur la grille
        long placementTime;          // Timestamp du placement
        int firepower;               // Puissance d'explosion de la bombe

        BombPosition(int x, int y, long time, int firepower) {
            this.x = x;                              // Coordonnée X
            this.y = y;                              // Coordonnée Y
            this.placementTime = time;               // Heure de placement
            this.firepower = firepower;              // Puissance d'explosion
        }

        /** Vérifie si la bombe a expiré et peut être oubliée */
        boolean isExpired(long currentTime) {
            // Calcule si le temps d'évitement est dépassé
            return currentTime - placementTime > BOMB_AVOIDANCE_DURATION;
        }

        /** Vérifie si une position est dans la zone d'explosion de cette bombe */
        boolean isInExplosionZone(int checkX, int checkY) {
            // Explosion horizontale (même ligne Y)
            if (checkY == y && Math.abs(checkX - x) <= firepower) {
                return true;                         // Position dans la zone horizontale
            }
            // Explosion verticale (même colonne X)
            if (checkX == x && Math.abs(checkY - y) <= firepower) {
                return true;                         // Position dans la zone verticale
            }
            return false;                            // Position hors de portée
        }
    }

    // ================ CONSTRUCTEUR & CONTRÔLE ================

    /**
     * Constructeur : Crée un bot contrôlant un joueur donné
     */
    public BotPlayer(Player player, GameBoard gameBoard) {
        // Initialiser le profil du bot
        this.botProfile = new PlayerProfile("Bot", "Bomberman", "BOT");
        this.botControlledPlayer = player;           // Sauvegarder le joueur à contrôler
        this.gameBoard = gameBoard;                  // Sauvegarder la référence du plateau
        System.out.println("Bot créé avec stratégie équilibrée intelligente");
    }

    /**
     * Active le bot : démarre le thread de décision
     */
    public void activate() {
        if (isActive) return;                        // Éviter les doubles activations

        isActive = true;                             // Marquer comme actif
        System.out.println("Bot activé - Joueur contrôlé: " + botControlledPlayer.getName());

        // Créer et démarrer le thread du bot
        botThread = new Thread(this::runBotLogic);   // Créer le thread avec la logique
        botThread.setDaemon(true);                   // Thread daemon (se ferme avec l'app)
        botThread.setName("Bot-Thread");             // Nommer le thread pour le debug
        botThread.start();                           // Démarrer le thread

        // Première décision immédiate
        Platform.runLater(this::makeDecision);       // Programmer la première décision
    }

    /**
     * Désactive le bot : arrête le thread de décision
     */
    public void deactivate() {
        if (!isActive) return;                       // Si déjà inactif, ne rien faire
        System.out.println("Désactivation du bot...");
        isActive = false;                            // Marquer comme inactif
        if (botThread != null) {
            botThread.interrupt();                   // Interrompre le thread
        }
    }

    // ================ LOGIQUE PRINCIPALE ================

    /**
     * BOUCLE PRINCIPALE : Exécute les décisions du bot à intervalles réguliers
     */
    private void runBotLogic() {
        // Boucle tant que le bot est actif
        while (isActive && !Thread.currentThread().isInterrupted()) {
            try {
                // Vérifier si le joueur est encore vivant
                if (botControlledPlayer.isAlive()) {
                    // Programmer une décision dans le thread JavaFX
                    Platform.runLater(this::makeDecision);
                }
                // Attendre avant la prochaine décision
                Thread.sleep(DECISION_DELAY);
            } catch (InterruptedException e) {
                // Thread interrompu = arrêt propre
                Thread.currentThread().interrupt();
                break;                               // Sortir de la boucle
            }
        }
    }

    /**
     * CŒUR DE L'IA : Prend une décision selon les priorités
     *
     * PRIORITÉS (dans l'ordre) :
     * 1. SURVIE : Fuir si en danger
     * 2. ATTAQUE : Placer bombe si stratégique
     * 3. MOUVEMENT : Se diriger vers objectif
     */
    private void makeDecision() {
        // Vérifier que le joueur existe et est vivant
        if (botControlledPlayer == null || !botControlledPlayer.isAlive()) {
            return;                                  // Arrêter si conditions non remplies
        }

        // Nettoyage des données expirées
        cleanExpiredBombs();                         // Supprimer les anciennes bombes
        cleanRecentActions();                        // Supprimer les anciennes actions

        // Récupérer la position actuelle du bot
        int botX = (int) Math.floor(botControlledPlayer.getX());
        int botY = (int) Math.floor(botControlledPlayer.getY());

        // PRIORITÉ 1 : SURVIE - Fuir les zones dangereuses
        if (isInExtendedDanger(botX, botY)) {
            System.out.println("BOT: ZONE DANGEREUSE - Fuite intelligente !");
            addRecentAction("ESCAPE_" + botX + "_" + botY); // Mémoriser l'action
            escapeFromExtendedDanger();              // Exécuter la fuite
            return;                                  // Une seule action par décision
        }

        // PRIORITÉ 2 : ATTAQUE - Placer bombe si avantageux
        if (shouldPlaceBombHere(botX, botY)) {
            System.out.println("BOT: Placement de bombe stratégique");
            addRecentAction("BOMB_" + botX + "_" + botY); // Mémoriser l'action
            placeBombAndEscape();                    // Placer la bombe et fuir
            return;                                  // Une seule action par décision
        }

        // PRIORITÉ 3 : MOUVEMENT - Chercher des objectifs
        System.out.println("BOT: Recherche d'objectif");
        addRecentAction("MOVE_" + botX + "_" + botY); // Mémoriser l'action
        moveIntelligently();                         // Se déplacer intelligemment
    }

    // ================ DÉTECTION DES DANGERS ================

    /**
     * DÉTECTION AVANCÉE : Vérifie si le bot est dans une zone dangereuse
     * Prend en compte :
     * - Toutes les bombes sur le plateau
     * - La puissance d'explosion variable (power-ups)
     * - Les obstacles qui bloquent les explosions
     */
    private boolean isInExtendedDanger(int botX, int botY) {
        // Vérifier toutes les bombes sur le plateau
        for (int x = 0; x < GameBoard.GRID_WIDTH; x++) {
            for (int y = 0; y < GameBoard.GRID_HEIGHT; y++) {
                // Si une bombe est présente à cette position
                if (gameBoard.getTile(x, y) == GameBoard.TileType.BOMB) {
                    // Estimer la puissance maximale pour la sécurité
                    int estimatedFirepower = Math.max(botControlledPlayer.getFirePower(), 3);

                    // Vérifier si le bot est dans le rayon de cette bombe
                    if (isInBombRange(botX, botY, x, y, estimatedFirepower)) {
                        return true;                 // Danger détecté
                    }
                }
            }
        }

        // Vérifier les bombes placées par le bot (puissance connue)
        for (BombPosition bomb : botPlacedBombs) {
            // Vérifier si le bot est dans la zone d'explosion
            if (bomb.isInExplosionZone(botX, botY)) {
                return true;                         // Danger des propres bombes
            }
        }

        return false;                                // Aucun danger détecté
    }

    /**
     * CALCUL PRÉCIS : Vérifie si une position est dans le rayon d'une bombe
     * Tient compte des murs qui bloquent l'explosion
     */
    private boolean isInBombRange(int posX, int posY, int bombX, int bombY, int firepower) {
        // Vérification horizontale (même ligne Y)
        if (posY == bombY && Math.abs(posX - bombX) <= firepower) {
            // Vérifier si l'explosion n'est pas bloquée
            return !isExplosionBlockedByWalls(bombX, bombY, posX, posY);
        }
        // Vérification verticale (même colonne X)
        if (posX == bombX && Math.abs(posY - bombY) <= firepower) {
            // Vérifier si l'explosion n'est pas bloquée
            return !isExplosionBlockedByWalls(bombX, bombY, posX, posY);
        }
        return false;                                // Position hors de portée
    }

    /**
     * PHYSIQUE DU JEU : Vérifie si l'explosion est bloquée par des murs
     */
    private boolean isExplosionBlockedByWalls(int fromX, int fromY, int toX, int toY) {
        // Calculer la direction de l'explosion
        int dx = Integer.signum(toX - fromX);
        int dy = Integer.signum(toY - fromY);

        // Commencer à la première case après la bombe
        int currentX = fromX + dx;
        int currentY = fromY + dy;

        // Parcourir le chemin de l'explosion
        while (currentX != toX || currentY != toY) {
            // Vérifier les limites de la carte
            if (currentX < 0 || currentX >= GameBoard.GRID_WIDTH ||
                    currentY < 0 || currentY >= GameBoard.GRID_HEIGHT) {
                return true; // Hors limites = explosion bloquée
            }

            // Vérifier le type de case
            GameBoard.TileType tile = gameBoard.getTile(currentX, currentY);
            if (tile == GameBoard.TileType.WALL || tile == GameBoard.TileType.BREAKABLE_WALL) {
                return true; // Mur = explosion bloquée
            }

            // Avancer vers la position cible
            currentX += dx;
            currentY += dy;
        }

        return false;                                // Chemin libre = explosion non bloquée
    }

    // ================ ÉVASION INTELLIGENTE ================

    /**
     * ÉVASION AVANCÉE : Trouve la position sûre la plus proche dans un rayon étendu
     * Algorithme : Recherche en losange par rayons croissants
     */
    private void escapeFromExtendedDanger() {
        // Récupérer la position actuelle
        int botX = (int) Math.floor(botControlledPlayer.getX());
        int botY = (int) Math.floor(botControlledPlayer.getY());

        // Variables pour la meilleure position trouvée
        int bestX = -1, bestY = -1;
        int minDistance = Integer.MAX_VALUE;

        // Recherche par rayons croissants (1, 2, 3, 4 cases)
        for (int radius = 1; radius <= 4; radius++) {
            // Parcourir toutes les positions à ce rayon
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    // Parcours en losange (distance Manhattan = radius)
                    if (Math.abs(dx) + Math.abs(dy) != radius) continue;

                    // Calculer la position à vérifier
                    int checkX = botX + dx;
                    int checkY = botY + dy;

                    // Vérifier si cette position est sûre
                    if (isSafePosition(checkX, checkY) && !isInExtendedDanger(checkX, checkY)) {
                        // Calculer la distance réelle
                        int distance = Math.abs(dx) + Math.abs(dy);
                        if (distance < minDistance) {
                            minDistance = distance;  // Nouvelle meilleure distance
                            bestX = checkX;          // Sauvegarder la position X
                            bestY = checkY;          // Sauvegarder la position Y
                        }
                    }
                }
            }
            if (bestX != -1) break; // Position trouvée, arrêter la recherche
        }

        // Se déplacer vers la position sûre trouvée
        if (bestX != -1) {
            // Calculer la meilleure direction
            Direction bestDir = getBestDirectionTo(botX, botY, bestX, bestY);
            if (bestDir != Direction.NONE) {
                // Calculer la prochaine position
                int newX = botX, newY = botY;
                switch (bestDir) {
                    case UP -> newY--;           // Aller vers le haut
                    case DOWN -> newY++;         // Aller vers le bas
                    case LEFT -> newX--;         // Aller vers la gauche
                    case RIGHT -> newX++;        // Aller vers la droite
                }

                // Vérifier que cette position est sûre
                if (isSafePosition(newX, newY)) {
                    moveToPosition(newX, newY);  // Effectuer le déplacement
                    System.out.println("BOT: Fuite intelligente réussie vers " + bestDir);
                    return;                      // Fuite réussie
                }
            }
        }

        // Plan de secours : évasion simple
        System.out.println("BOT: Fuite d'urgence !");
        escapeFromDanger();                          // Utiliser la méthode de secours
    }

    /**
     * ÉVASION SIMPLE : Trouve la première direction sûre (plan de secours)
     */
    private void escapeFromDanger() {
        // Récupérer la position actuelle
        int botX = (int) Math.floor(botControlledPlayer.getX());
        int botY = (int) Math.floor(botControlledPlayer.getY());

        // Préparer toutes les directions possibles
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        shuffleArray(directions); // Randomiser pour éviter les patterns

        // Tester chaque direction
        for (Direction dir : directions) {
            // Calculer la nouvelle position
            int newX = botX, newY = botY;
            switch (dir) {
                case UP -> newY--;               // Aller vers le haut
                case DOWN -> newY++;             // Aller vers le bas
                case LEFT -> newX--;             // Aller vers la gauche
                case RIGHT -> newX++;            // Aller vers la droite
            }

            // Vérifier si cette position est sûre
            if (isSafePosition(newX, newY) && !isInExtendedDanger(newX, newY)) {
                moveToPosition(newX, newY);      // Effectuer le déplacement
                System.out.println("BOT: Fuite d'urgence vers " + dir);
                return;                          // Fuite réussie
            }
        }

        System.out.println("BOT: Aucune échappatoire trouvée !");
    }

    // ================ STRATÉGIE DE BOMBE ================

    /**
     * ANALYSE STRATÉGIQUE : Détermine s'il faut placer une bombe ici
     * Conditions :
     * - Cooldown respecté
     * - Capacité de placement disponible
     * - Cibles (murs cassables) à portée
     * - Possibilité d'évasion
     */
    private boolean shouldPlaceBombHere(int x, int y) {
        // Vérifications de base
        long currentTime = System.currentTimeMillis();
        // Vérifier le cooldown et la capacité
        if (currentTime - lastBombTime < BOMB_COOLDOWN || !botControlledPlayer.canPlaceBomb()) {
            return false;                        // Conditions non remplies
        }

        // Vérifier qu'il n'y a pas déjà une bombe
        if (gameBoard.getTile(x, y) == GameBoard.TileType.BOMB) {
            return false; // Déjà une bombe ici
        }

        // Vérifier s'il y a des cibles valides
        int firepower = botControlledPlayer.getFirePower();
        boolean hasTarget = hasValidTargetsInRange(x, y, firepower);

        // Placer seulement s'il y a des cibles ET possibilité d'évasion
        return hasTarget && canEscapeFromPosition(x, y);
    }

    /**
     * DÉTECTION DE CIBLES : Cherche des murs cassables dans le rayon d'action
     */
    private boolean hasValidTargetsInRange(int x, int y, int firepower) {
        // Vérifier dans les 4 directions
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};

        // Tester chaque direction
        for (Direction dir : directions) {
            // Vérifier chaque case dans la portée
            for (int i = 1; i <= firepower; i++) {
                // Calculer la position à vérifier
                int checkX = x, checkY = y;

                switch (dir) {
                    case UP -> checkY = y - i;   // Vérifier vers le haut
                    case DOWN -> checkY = y + i; // Vérifier vers le bas
                    case LEFT -> checkX = x - i; // Vérifier vers la gauche
                    case RIGHT -> checkX = x + i; // Vérifier vers la droite
                }

                // Vérifier les limites de la carte
                if (checkX < 0 || checkX >= GameBoard.GRID_WIDTH ||
                        checkY < 0 || checkY >= GameBoard.GRID_HEIGHT) {
                    break;                       // Hors limites, arrêter cette direction
                }

                // Vérifier le type de case
                GameBoard.TileType tile = gameBoard.getTile(checkX, checkY);
                if (tile == GameBoard.TileType.BREAKABLE_WALL) {
                    return true; // Cible trouvée !
                }
                if (tile == GameBoard.TileType.WALL) {
                    break; // Mur solide = arrêt de l'explosion
                }
            }
        }
        return false;                            // Aucune cible trouvée
    }

    /**
     * VÉRIFICATION D'ÉVASION : S'assure qu'il y a au moins une sortie
     */
    private boolean canEscapeFromPosition(int bombX, int bombY) {
        // Vérifier toutes les directions possibles
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};

        // Tester chaque direction d'évasion
        for (Direction dir : directions) {
            // Calculer la position d'évasion
            int escapeX = bombX, escapeY = bombY;
            switch (dir) {
                case UP -> escapeY--;            // Évasion vers le haut
                case DOWN -> escapeY++;          // Évasion vers le bas
                case LEFT -> escapeX--;          // Évasion vers la gauche
                case RIGHT -> escapeX++;         // Évasion vers la droite
            }

            // Vérifier si cette position est sûre
            if (isSafePosition(escapeX, escapeY)) {
                return true; // Au moins une sortie disponible
            }
        }

        return false;                            // Aucune sortie disponible
    }

    /**
     * PLACEMENT ET ÉVASION : Place la bombe et s'échappe immédiatement
     */
    private void placeBombAndEscape() {
        // Récupérer la position actuelle
        int gridX = (int) Math.floor(botControlledPlayer.getX());
        int gridY = (int) Math.floor(botControlledPlayer.getY());
        int firepower = botControlledPlayer.getFirePower();

        // Créer et placer la bombe
        Bomb bomb = new Bomb(gridX, gridY, firepower, botControlledPlayer.getPlayerId());
        gameBoard.addBomb(bomb);                 // Ajouter au plateau
        botControlledPlayer.incrementBombsPlaced(); // Incrémenter le compteur

        // Mémoriser cette bombe pour l'éviter
        botPlacedBombs.add(new BombPosition(gridX, gridY, System.currentTimeMillis(), firepower));
        lastBombTime = System.currentTimeMillis(); // Mettre à jour le timestamp

        System.out.println("BOT: BOMBE placée à (" + gridX + ", " + gridY + ") puissance " + firepower);

        // Évasion immédiate
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        shuffleArray(directions);                // Randomiser les directions

        // Chercher une direction d'évasion
        for (Direction dir : directions) {
            // Calculer la position d'évasion
            int escapeX = gridX, escapeY = gridY;
            switch (dir) {
                case UP -> escapeY--;            // Évasion vers le haut
                case DOWN -> escapeY++;          // Évasion vers le bas
                case LEFT -> escapeX--;          // Évasion vers la gauche
                case RIGHT -> escapeX++;         // Évasion vers la droite
            }

            // Vérifier si cette position est sûre
            if (isSafePosition(escapeX, escapeY)) {
                moveToPosition(escapeX, escapeY); // Effectuer l'évasion
                System.out.println("BOT: Évasion réussie vers " + dir);
                return;                          // Évasion réussie
            }
        }

        System.out.println("BOT: Placement de bombe sans évasion possible");
    }

    // ================ MOUVEMENT INTELLIGENT ================

    /**
     * MOUVEMENT STRATÉGIQUE : Cherche des objectifs intéressants
     * Priorité : Murs cassables > Exploration
     */
    private void moveIntelligently() {
        // Récupérer la position actuelle
        int botX = (int) Math.floor(botControlledPlayer.getX());
        int botY = (int) Math.floor(botControlledPlayer.getY());

        // Chercher le mur cassable le plus proche
        int closestX = -1, closestY = -1;
        int minDistance = Integer.MAX_VALUE;

        // Parcourir toute la carte
        for (int x = 0; x < GameBoard.GRID_WIDTH; x++) {
            for (int y = 0; y < GameBoard.GRID_HEIGHT; y++) {
                // Si un mur cassable est trouvé
                if (gameBoard.getTile(x, y) == GameBoard.TileType.BREAKABLE_WALL) {
                    // Calculer la distance Manhattan
                    int distance = Math.abs(x - botX) + Math.abs(y - botY);
                    if (distance < minDistance) {
                        minDistance = distance;  // Nouvelle distance minimale
                        closestX = x;            // Sauvegarder position X
                        closestY = y;            // Sauvegarder position Y
                    }
                }
            }
        }

        // Si un mur cassable a été trouvé
        if (closestX != -1) {
            // Se diriger vers ce mur
            moveTowardsTarget(botX, botY, closestX, closestY, "mur cassable");
        } else {
            // Pas de mur cassable, explorer le centre
            exploreMap();
        }
    }

    /**
     * MOUVEMENT VERS CIBLE : Se dirige vers un objectif en évitant les répétitions
     */
    private void moveTowardsTarget(int fromX, int fromY, int targetX, int targetY, String targetType) {
        // Calculer la meilleure direction vers la cible
        Direction bestDir = getBestDirectionTo(fromX, fromY, targetX, targetY);

        // Si une direction valide a été trouvée
        if (bestDir != Direction.NONE) {
            // Calculer la nouvelle position
            int newX = fromX, newY = fromY;
            switch (bestDir) {
                case UP -> newY--;               // Aller vers le haut
                case DOWN -> newY++;             // Aller vers le bas
                case LEFT -> newX--;             // Aller vers la gauche
                case RIGHT -> newX++;            // Aller vers la droite
            }

            // Créer une clé pour cette action
            String moveAction = "MOVE_" + newX + "_" + newY;

            // Vérifier que le mouvement est sûr et non répétitif
            if (isSafePosition(newX, newY) &&
                    !isInExtendedDanger(newX, newY) &&
                    !hasRecentAction(moveAction)) {

                moveToPosition(newX, newY);      // Effectuer le déplacement
                System.out.println("BOT: Avance vers " + targetType + " (" + targetX + "," + targetY + ") - " + bestDir);
                return;                          // Mouvement réussi
            }
        }

        // Plan de secours : mouvement aléatoire intelligent
        moveRandomlySmartly();
    }

    /**
     * EXPLORATION : Se dirige vers le centre de la carte
     */
    private void exploreMap() {
        // Récupérer la position actuelle
        int botX = (int) Math.floor(botControlledPlayer.getX());
        int botY = (int) Math.floor(botControlledPlayer.getY());

        // Calculer le centre de la carte
        int centerX = GameBoard.GRID_WIDTH / 2;
        int centerY = GameBoard.GRID_HEIGHT / 2;

        // Se diriger vers le centre
        moveTowardsTarget(botX, botY, centerX, centerY, "centre de la carte");
    }

    /**
     * MOUVEMENT ALÉATOIRE INTELLIGENT : Évite les répétitions et les dangers
     */
    private void moveRandomlySmartly() {
        // Récupérer la position actuelle
        int botX = (int) Math.floor(botControlledPlayer.getX());
        int botY = (int) Math.floor(botControlledPlayer.getY());

        // Préparer toutes les directions
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        shuffleArray(directions);                // Randomiser l'ordre

        // Tester chaque direction
        for (Direction dir : directions) {
            // Calculer la nouvelle position
            int newX = botX, newY = botY;
            switch (dir) {
                case UP -> newY--;               // Aller vers le haut
                case DOWN -> newY++;             // Aller vers le bas
                case LEFT -> newX--;             // Aller vers la gauche
                case RIGHT -> newX++;            // Aller vers la droite
            }

            // Créer une clé pour cette action
            String moveAction = "MOVE_" + newX + "_" + newY;

            // Vérifier que le mouvement est sûr et non répétitif
            if (isSafePosition(newX, newY) &&
                    !isInExtendedDanger(newX, newY) &&
                    !hasRecentAction(moveAction)) {

                moveToPosition(newX, newY);      // Effectuer le déplacement
                System.out.println("BOT: Mouvement intelligent aléatoire - " + dir);
                return;                          // Mouvement réussi
            }
        }

        System.out.println("BOT: Aucun mouvement intelligent possible");
    }

    // ================ UTILITAIRES ================

    /**
     * CALCUL DE DIRECTION : Trouve la meilleure direction vers une cible
     */
    private Direction getBestDirectionTo(int fromX, int fromY, int toX, int toY) {
        // Calculer les différences de coordonnées
        int dx = toX - fromX;
        int dy = toY - fromY;

        // Prioriser la direction avec la plus grande distance
        if (Math.abs(dx) > Math.abs(dy)) {
            // Distance horizontale plus grande
            return dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else if (dy != 0) {
            // Distance verticale plus grande ou égale
            return dy > 0 ? Direction.DOWN : Direction.UP;
        }

        return Direction.NONE;                   // Déjà à la destination
    }

    /**
     * VÉRIFICATION DE SÉCURITÉ : Détermine si une position est traversable et sûre
     */
    private boolean isSafePosition(int x, int y) {
        // Vérifier les limites de la carte
        if (x < 0 || x >= GameBoard.GRID_WIDTH || y < 0 || y >= GameBoard.GRID_HEIGHT) {
            return false;                        // Position hors limites
        }

        // Vérifier les obstacles sur cette case
        GameBoard.TileType tile = gameBoard.getTile(x, y);
        if (tile == GameBoard.TileType.WALL ||
                tile == GameBoard.TileType.BREAKABLE_WALL ||
                tile == GameBoard.TileType.BOMB) {
            return false;                        // Case obstruée
        }

        // Éviter les positions des bombes du bot
        for (BombPosition bomb : botPlacedBombs) {
            if (bomb.x == x && bomb.y == y) {
                return false;                    // Position occupée par une bombe du bot
            }
        }

        return true;                             // Position sûre et traversable
    }

    /**
     * DÉPLACEMENT PHYSIQUE : Change la position du joueur contrôlé
     */
    private void moveToPosition(int gridX, int gridY) {
        // Convertir les coordonnées grille en coordonnées réelles
        double targetX = gridX + 0.5;            // Centrer sur la case X
        double targetY = gridY + 0.5;            // Centrer sur la case Y
        // Appliquer la nouvelle position au joueur
        botControlledPlayer.setPosition(targetX, targetY);
    }

    // ================ GESTION DE LA MÉMOIRE ================

    /**
     * PRÉVENTION DES BOUCLES : Mémorise une action récente
     */
    private void addRecentAction(String action) {
        recentActions.add(action);               // Ajouter l'action à la liste
        // Maintenir la taille de la liste sous la limite
        if (recentActions.size() > MAX_RECENT_ACTIONS) {
            recentActions.remove(0);             // Supprimer la plus ancienne
        }
    }

    /**
     * VÉRIFICATION D'HISTORIQUE : Vérifie si une action a été faite récemment
     */
    private boolean hasRecentAction(String action) {
        // Chercher cette action dans la liste des actions récentes
        return recentActions.contains(action);
    }

    /**
     * NETTOYAGE MÉMOIRE : Supprime les actions trop anciennes
     */
    private void cleanRecentActions() {
        // Maintenir la taille de la liste sous la limite
        while (recentActions.size() > MAX_RECENT_ACTIONS) {
            recentActions.remove(0);             // Supprimer la plus ancienne
        }
    }

    /**
     * NETTOYAGE BOMBES : Supprime les bombes expirées de la mémoire
     */
    private void cleanExpiredBombs() {
        long currentTime = System.currentTimeMillis();
        // Supprimer toutes les bombes expirées de la liste
        botPlacedBombs.removeIf(bomb -> bomb.isExpired(currentTime));
    }

    /**
     * RANDOMISATION : Mélange un tableau (algorithme de Fisher-Yates)
     */
    private void shuffleArray(Direction[] array) {
        // Parcourir le tableau de la fin vers le début
        for (int i = array.length - 1; i > 0; i--) {
            // Choisir un index aléatoire entre 0 et i
            int index = random.nextInt(i + 1);
            // Échanger les éléments aux positions i et index
            Direction temp = array[index];
            array[index] = array[i];             // Placer l'élément i à la position index
            array[i] = temp;                     // Placer l'élément index à la position i
        }
    }

    /**
     * ACCESSEUR : Retourne le profil du bot
     */
    public PlayerProfile getProfile() {
        return botProfile;                       // Retourner le profil du bot
    }
}