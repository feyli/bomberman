package fr.amu.iut.bomberman.view;

import fr.amu.iut.bomberman.model.*;
import fr.amu.iut.bomberman.utils.Direction;
import fr.amu.iut.bomberman.utils.ThemeManager;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe responsable du rendu graphique du jeu
 * Dessine tous les éléments visuels sur le canvas
 *
 * @author Super Bomberman Team
 * @version 1.0
 */
public class GameRenderer {

    private final Canvas canvas;
    private final GraphicsContext gc;

    // Cache des images
    private Map<String, Image> imageCache;
    private ThemeManager themeManager;

    // Dimensions de rendu
    private double tileSize;
    private double offsetX;
    private double offsetY;

    // Couleurs de remplacement pour les sprites manquants
    private static final Color FLOOR_COLOR = Color.rgb(50, 50, 50);
    private static final Color WALL_COLOR = Color.GRAY;
    private static final Color BREAKABLE_COLOR = Color.rgb(139, 69, 19);
    private static final Color PLAYER1_COLOR = Color.BLUE;
    private static final Color PLAYER2_COLOR = Color.RED;
    private static final Color BOMB_COLOR = Color.rgb(255, 140, 0);
    private static final Color EXPLOSION_COLOR = Color.YELLOW;
    private static final Color POWERUP_COLOR = Color.LIMEGREEN;

    // Compteur pour l'animation
    private double animationTimer = 0;

    /**
     * Constructeur du renderer
     *
     * @param canvas Canvas de rendu
     */
    public GameRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.imageCache = new HashMap<>();
        this.themeManager = ThemeManager.getInstance();

        loadImages();
    }

    /**
     * Charge toutes les images nécessaires (avec gestion d'erreurs)
     */
    private void loadImages() {
        // Essayer de charger les images, mais continuer même si elles manquent
        loadImageSafe("empty", "/images/tiles/empty.jpg");
        loadImageSafe("wall", "/images/tiles/wall.png");
        loadImageSafe("breakable", "/images/tiles/breakable.png");

        // Charger l'arrière-plan du jeu basé sur le thème actuel
        loadImageSafe("background", themeManager.getBackgroundImagePath());

        // Joueurs
        for (int i = 1; i <= 2; i++) {
            loadImageSafe("player" + i + "_down", "/images/players/player" + i + "_downward.gif");
            loadImageSafe("player" + i + "_up", "/images/players/player" + i + "_upward.gif");
            loadImageSafe("player" + i + "_left", "/images/players/player" + i + "_left.gif");
            loadImageSafe("player" + i + "_right", "/images/players/player" + i + "_right.gif");
        }

        // Bombes
        for (int i = 0; i < 3; i++) {
            loadImageSafe("bomb_" + i, "/images/bombs/bomb_" + i + ".png");
        }

        // Explosions
        loadImageSafe("explosion_center", "/images/explosions/center.png");
        loadImageSafe("explosion_horizontal", "/images/explosions/horizontal.png");
        loadImageSafe("explosion_vertical", "/images/explosions/vertical.png");
        loadImageSafe("explosion_end_up", "/images/explosions/end_up.png");
        loadImageSafe("explosion_end_down", "/images/explosions/end_down.png");
        loadImageSafe("explosion_end_left", "/images/explosions/end_left.png");
        loadImageSafe("explosion_end_right", "/images/explosions/end_right.png");

        // Power-ups
        loadImageSafe("powerup_bomb", "/images/powerups/bomb_up.png");
        loadImageSafe("powerup_fire", "/images/powerups/fire_up.png");
        loadImageSafe("powerup_speed", "/images/powerups/speed_up.png");
        loadImageSafe("powerup_life", "/images/powerups/life.png");
        loadImageSafe("powerup_kick", "/images/powerups/kick.png");
        loadImageSafe("powerup_remote", "/images/powerups/remote.png");


        System.out.println("Images chargées. Utilisation de couleurs de remplacement pour les sprites manquants.");
    }

    /**
     * Charge une image de manière sécurisée
     */
    private void loadImageSafe(String key, String path) {
        try {
            if (getClass().getResource(path) != null) {
                Image image = new Image(getClass().getResourceAsStream(path));
                imageCache.put(key, image);
                System.out.println("Image chargée: " + key);
            } else {
                System.out.println("Image manquante: " + path + " (utilisation de couleur de remplacement)");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de " + path + ": " + e.getMessage());
        }
    }

    /**
     * Effectue le rendu complet du jeu
     */
    public void render(GameModel gameModel) {
        if (gameModel == null) return;

        // Mettre à jour l'animation
        animationTimer += 0.016; // ~60 FPS

        calculateDimensions();

        // Effacer le canvas
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Dessiner l'arrière-plan
        renderBackground();

        // Dessiner le plateau
        renderBoard(gameModel.getGameBoard());

        // Dessiner les power-ups
        renderPowerUps(gameModel.getGameBoard());

        // Dessiner les bombes
        renderBombs(gameModel.getGameBoard());

        // Dessiner les explosions
        renderExplosions(gameModel.getGameBoard());

        // Dessiner les joueurs
        renderPlayers(gameModel);

        // Dessiner l'interface de jeu
        renderGameUI(gameModel);

        // Debug: afficher la grille (optionnel)
        if (false) { // Mettre à true pour debug
            renderDebugGrid();
        }
    }

    /**
     * Dessine l'arrière-plan du jeu
     */
    private void renderBackground() {
        Image backgroundImage = imageCache.get("background");
        if (backgroundImage != null) {
            // Dessiner l'image d'arrière-plan en l'adaptant aux dimensions du canvas
            gc.drawImage(backgroundImage, 0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }

    /**
     * Calcule les dimensions de rendu
     */
    private void calculateDimensions() {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        if (canvasWidth <= 0 || canvasHeight <= 0) {
            canvasWidth = 800;
            canvasHeight = 600;
        }

        // Calculer la taille des tuiles
        double tileSizeX = canvasWidth / GameBoard.GRID_WIDTH;
        double tileSizeY = canvasHeight / GameBoard.GRID_HEIGHT;
        tileSize = Math.min(tileSizeX, tileSizeY);

        // Centrer le plateau
        offsetX = (canvasWidth - (GameBoard.GRID_WIDTH * tileSize)) / 2;
        offsetY = (canvasHeight - (GameBoard.GRID_HEIGHT * tileSize)) / 2;
    }

    /**
     * Dessine le plateau de jeu
     */
    private void renderBoard(GameBoard board) {
        for (int x = 0; x < GameBoard.GRID_WIDTH; x++) {
            for (int y = 0; y < GameBoard.GRID_HEIGHT; y++) {
                double drawX = offsetX + x * tileSize;
                double drawY = offsetY + y * tileSize;

                // Toujours dessiner le sol
                drawTile(drawX, drawY, FLOOR_COLOR, "empty");

                // Dessiner les murs
                GameBoard.TileType tile = board.getTile(x, y);
                switch (tile) {
                    case WALL:
                        drawTile(drawX, drawY, WALL_COLOR, "wall");
                        break;
                    case BREAKABLE_WALL:
                        drawTile(drawX, drawY, BREAKABLE_COLOR, "breakable");
                        break;
                }
            }
        }
    }

    /**
     * Dessine les power-ups
     */
    private void renderPowerUps(GameBoard board) {
        for (PowerUp powerUp : board.getPowerUps()) {
            double drawX = offsetX + powerUp.getX() * tileSize;
            double drawY = offsetY + powerUp.getY() * tileSize;

            // Animation pulse
            double scale = 1.0 + Math.sin(animationTimer * 3) * 0.1;
            double sizeOffset = (1 - scale) * tileSize / 2;

            String imageKey = getPowerUpImageKey(powerUp.getType());

            gc.save();
            gc.translate(drawX + tileSize / 2, drawY + tileSize / 2);
            gc.scale(scale, scale);
            gc.translate(-tileSize / 2, -tileSize / 2);

            drawTile(0, 0, POWERUP_COLOR, imageKey);

            gc.restore();
        }
    }

    /**
     * Dessine les bombes
     */
    private void renderBombs(GameBoard board) {
        for (Bomb bomb : board.getBombs()) {
            double drawX = offsetX + bomb.getX() * tileSize;
            double drawY = offsetY + bomb.getY() * tileSize;

            // Animation de la bombe (pulsation)
            double timePercent = bomb.getTimePercentage();
            double pulseSpeed = 5.0 + (1.0 - timePercent) * 10.0; // Accélère près de l'explosion
            double scale = 1.0 + Math.sin(animationTimer * pulseSpeed) * 0.2 * (1.0 - timePercent);

            gc.save();
            gc.translate(drawX + tileSize / 2, drawY + tileSize / 2);
            gc.scale(scale, scale);
            gc.translate(-tileSize / 2, -tileSize / 2);

            // Couleur qui devient rouge près de l'explosion
            Color bombColor = Color.rgb(
                    255,
                    (int) (140 * timePercent),
                    0
            );

            drawTile(0, 0, bombColor, "bomb_0");

            gc.restore();
        }
    }

    /**
     * Dessine les explosions
     */
    private void renderExplosions(GameBoard board) {
        for (Explosion explosion : board.getExplosions()) {
            double drawX = offsetX + explosion.getX() * tileSize;
            double drawY = offsetY + explosion.getY() * tileSize;

            // Couleur qui varie selon l'intensité
            double intensity = explosion.getIntensity();
            Color explosionColor = Color.color(1.0, 1.0 * intensity, 0.0, 0.8 * intensity);

            String imageKey = getExplosionImageKey(explosion.getType());

            // Animation d'expansion
            double scale = 1.0 + (1.0 - intensity) * 0.3;

            gc.save();
            gc.translate(drawX + tileSize / 2, drawY + tileSize / 2);
            gc.scale(scale, scale);
            gc.translate(-tileSize / 2, -tileSize / 2);

            drawTile(0, 0, explosionColor, imageKey);

            gc.restore();
        }
    }

    /**
     * Obtient la clé d'image pour un type d'explosion
     */
    private String getExplosionImageKey(Explosion.Type type) {
        return switch (type) {
            case CENTER -> "explosion_center";
            case HORIZONTAL -> "explosion_horizontal";
            case VERTICAL -> "explosion_vertical";
            case END_UP -> "explosion_end_up";
            case END_DOWN -> "explosion_end_down";
            case END_LEFT -> "explosion_end_left";
            case END_RIGHT -> "explosion_end_right";
        };
    }

    /**
     * Dessine les joueurs
     */
    private void renderPlayers(GameModel gameModel) {
        // Joueur 1
        Player player1 = gameModel.getPlayer1();
        if (player1 != null && player1.isAlive()) {
            renderPlayer(player1, PLAYER1_COLOR, 1);
        }

        // Joueur 2
        Player player2 = gameModel.getPlayer2();
        if (player2 != null && player2.isAlive()) {
            renderPlayer(player2, PLAYER2_COLOR, 2);
        }
    }

    /**
     * Dessine un joueur
     */
    private void renderPlayer(Player player, Color color, int playerNumber) {
        // Position du joueur centrée sur sa case
        double drawX = offsetX + (player.getX() - 0.5) * tileSize;
        double drawY = offsetY + (player.getY() - 0.5) * tileSize;

        // Déterminer l'image selon la direction
        String direction = switch (player.getCurrentDirection()) {
            case UP -> "up";
            case DOWN, NONE -> "down";
            case LEFT -> "left";
            case RIGHT -> "right";
        };

        String imageKey = "player" + playerNumber + "_" + direction;

        // Dessiner le joueur (légèrement plus petit que la tuile)
        double playerSize = tileSize * 0.8;
        double playerOffset = (tileSize - playerSize) / 2;

        // Animation de marche
        double walkOffset = 0;
        if (player.getCurrentDirection() != Direction.NONE) {
            walkOffset = Math.sin(animationTimer * 10) * 2;
        }

        drawPlayerTile(drawX + playerOffset, drawY + playerOffset + walkOffset,
                playerSize, color, imageKey, playerNumber);
    }

    /**
     * Dessine l'interface utilisateur du jeu
     */
    private void renderGameUI(GameModel gameModel) {
        if (gameModel.getGameState() == GameModel.GameState.PAUSED) {
            renderCenteredText("PAUSE", 48, Color.YELLOW);
        } else if (gameModel.getGameState() == GameModel.GameState.ROUND_OVER) {
            renderCenteredText("Round Terminé!", 36, Color.WHITE);
        } else if (gameModel.getGameState() == GameModel.GameState.GAME_OVER) {
            renderCenteredText("Game Over!", 48, Color.RED);
        }
    }

    /**
     * Dessine une tuile (avec image ou couleur de remplacement)
     */
    private void drawTile(double x, double y, Color fallbackColor, String imageKey) {
        Image image = imageCache.get(imageKey);
        if (image != null) {
            gc.drawImage(image, x, y, tileSize, tileSize);
        } else {
            // Utiliser la couleur de remplacement
            gc.setFill(fallbackColor);
            gc.fillRect(x, y, tileSize, tileSize);

            // Ajouter une bordure pour plus de clarté
            gc.setStroke(Color.rgb(30, 30, 30));
            gc.setLineWidth(1);
            gc.strokeRect(x, y, tileSize, tileSize);
        }
    }

    /**
     * Dessine une tuile de joueur
     */
    private void drawPlayerTile(double x, double y, double size, Color color,
                                String imageKey, int playerNumber) {
        Image image = imageCache.get(imageKey);
        if (image != null) {
            gc.drawImage(image, x, y, size, size);
        } else {
            // Dessiner un cercle coloré pour le joueur
            gc.setFill(color);
            gc.fillOval(x, y, size, size);

            // Bordure
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeOval(x, y, size, size);

            // Numéro du joueur
            gc.setFill(Color.WHITE);
            gc.setFont(new Font("Arial Black", size * 0.4));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.valueOf(playerNumber), x + size / 2, y + size / 2 + size * 0.15);
        }
    }

    /**
     * Dessine du texte centré
     */
    private void renderCenteredText(String text, int size, Color color) {
        gc.setFont(new Font("Arial Black", size));
        gc.setFill(color);
        gc.setTextAlign(TextAlignment.CENTER);

        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;

        gc.fillText(text, centerX, centerY);

        // Contour noir
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(3);
        gc.strokeText(text, centerX, centerY);
    }

    /**
     * Obtient la clé d'image pour un type de power-up
     */
    private String getPowerUpImageKey(PowerUp.Type type) {
        return switch (type) {
            case BOMB_UP -> "powerup_bomb";
            case FIRE_UP -> "powerup_fire";
            case SPEED_UP -> "powerup_speed";
            case KICK -> "powerup_kick";
            case REMOTE_CONTROL -> "powerup_remote";
            case EXTRA_LIFE -> "powerup_life";
        };
    }

    /**
     * Dessine la grille de debug
     */
    private void renderDebugGrid() {
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(0.5);
        gc.setGlobalAlpha(0.3);

        // Lignes verticales
        for (int x = 0; x <= GameBoard.GRID_WIDTH; x++) {
            double drawX = offsetX + x * tileSize;
            gc.strokeLine(drawX, offsetY, drawX, offsetY + GameBoard.GRID_HEIGHT * tileSize);
        }

        // Lignes horizontales
        for (int y = 0; y <= GameBoard.GRID_HEIGHT; y++) {
            double drawY = offsetY + y * tileSize;
            gc.strokeLine(offsetX, drawY, offsetX + GameBoard.GRID_WIDTH * tileSize, drawY);
        }

        // Numéros de grille
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial", 10));
        gc.setTextAlign(TextAlignment.CENTER);

        for (int x = 0; x < GameBoard.GRID_WIDTH; x++) {
            for (int y = 0; y < GameBoard.GRID_HEIGHT; y++) {
                double drawX = offsetX + (x + 0.5) * tileSize;
                double drawY = offsetY + (y + 0.5) * tileSize;
                gc.fillText(x + "," + y, drawX, drawY);
            }
        }

        gc.setGlobalAlpha(1.0);
    }
}

