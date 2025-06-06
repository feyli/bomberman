package fr.amu.iut.bomberman;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameView {
    @FXML
    private GridPane gameGrid;
    @FXML
    private HBox playerInfo;
    @FXML
    private Label gameTimer;
    @FXML
    private VBox pauseMenu;
    @FXML
    private VBox gameOverScreen;
    @FXML
    private Label winnerLabel;
    @FXML
    private VBox mainMenu;
    @FXML
    private VBox gameInterface;
    @FXML
    private VBox controlsScreen;
    @FXML
    private HBox player1Info;
    @FXML
    private HBox player2Info;
    @FXML
    private HBox player3Info;
    @FXML
    private HBox player4Info;
    @FXML
    private Label player1BombCount;
    @FXML
    private Label player2BombCount;
    @FXML
    private Label player3BombCount;
    @FXML
    private Label player4BombCount;
    @FXML
    private ImageView player1Icon;
    @FXML
    private ImageView player2Icon;
    @FXML
    private ImageView player3Icon;
    @FXML
    private ImageView player4Icon;
    @FXML
    private ImageView player1BombIcon;
    @FXML
    private ImageView player2BombIcon;
    @FXML
    private ImageView player3BombIcon;
    @FXML
    private ImageView player4BombIcon;

    // Variables pour les icônes des joueurs
    private Image[] playerIconImages;
    private Image bombIconImage;
    private ImageView[][] cellViews;
    private GameController controller;

    // Images pour les différents éléments
    private Image emptyImage;
    private Image wallImage;
    private Image destructibleWallImage;
    private Image bombImage;
    private Image explosionImage;
    private Image[][] playerDirectionalImages;
    private Image[] playerImages;
    private Image[] powerUpImages;

    private Map<Integer, Direction> playerDirections = new HashMap<>();
    private Map<String, Image> imageCache = new HashMap<>();

    // NOUVEAU : Suivi des bombes par joueur
    private Map<Integer, Integer> playerActiveBombs = new HashMap<>();
    private Map<Integer, Integer> playerMaxBombs = new HashMap<>();

    public void initialize() {
        setupGameGrid();
        loadImages();
        setupUI();

        // Initialiser le suivi des bombes
        for (int i = 1; i <= 4; i++) {
            playerActiveBombs.put(i, 0);
            playerMaxBombs.put(i, 1); // Valeur par défaut
        }
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }

    @FXML
    private void onStartGame3Players() {
        if (controller != null) {
            controller.startNewGame(3);
            initializeGameHUD(3);
            hideMainMenu();
            showGameInterface();
        }
    }

    @FXML
    private void onStartGame4Players() {
        if (controller != null) {
            controller.startNewGame(4);
            initializeGameHUD(4);
            hideMainMenu();
            showGameInterface();
        }
    }

    @FXML
    private void onStartGame() {
        if (controller != null) {
            controller.startNewGame(2);
            initializeGameHUD(2);
            hideMainMenu();
            showGameInterface();
        }
    }

    @FXML
    private void onHideControls() {
        hideControlsScreen();
        showMainMenu();
    }

    private void showGameInterface() {
        if (gameInterface != null) {
            gameInterface.setVisible(true);
        }
    }

    private void hideGameInterface() {
        if (gameInterface != null) {
            gameInterface.setVisible(false);
        }
    }

    private void showControlsScreen() {
        if (controlsScreen != null) {
            controlsScreen.setVisible(true);
        }
    }

    private void hideControlsScreen() {
        if (controlsScreen != null) {
            controlsScreen.setVisible(false);
        }
    }

    public void forceUpdateHUD() {
        if (controller != null && controller.getGame() != null) {
            List<Player> players = controller.getGame().getPlayers();
            updateBombermanHUD(players);
        }
    }

    // NOUVELLE MÉTHODE : Compter les bombes actives du joueur spécifique
    private int countPlayerActiveBombs(int playerId) {
        if (controller == null || controller.getGame() == null) {
            return playerActiveBombs.getOrDefault(playerId, 0);
        }

        try {
            GameBoard board = controller.getGame().getBoard();
            if (board == null) return playerActiveBombs.getOrDefault(playerId, 0);

            Cell[][] grid = board.getGrid();
            int activeBombs = 0;

            // Parcourir la grille pour compter les bombes du joueur
            for (int x = 0; x < Constants.BOARD_WIDTH; x++) {
                for (int y = 0; y < Constants.BOARD_HEIGHT; y++) {
                    Cell cell = grid[x][y];
                    if (cell.getType() == CellType.BOMB) {
                        // Ici, vous devrez adapter selon votre implémentation
                        // pour identifier le propriétaire de la bombe
                        // Si vous avez une méthode cell.getBombOwnerId() ou similaire
                        activeBombs++;
                    }
                }
            }

            // Mettre à jour le cache local
            playerActiveBombs.put(playerId, activeBombs);
            return activeBombs;

        } catch (Exception e) {
            System.err.println("Erreur comptage bombes actives pour joueur " + playerId + ": " + e.getMessage());
            return playerActiveBombs.getOrDefault(playerId, 0);
        }
    }

    // CORRECTION : Méthodes pour gérer les bombes correctement
    public void onBombPlaced(int playerId) {
        System.out.println("Bombe posée par joueur " + playerId);

        // Incrémenter le compteur local
        int currentActive = playerActiveBombs.getOrDefault(playerId, 0);
        playerActiveBombs.put(playerId, currentActive + 1);

        // Mettre à jour SEULEMENT ce joueur
        updateSinglePlayerHUD(playerId);
    }

    public void onBombExploded(int playerId) {
        System.out.println("Bombe explosée pour joueur " + playerId);

        // Décrémenter le compteur local
        int currentActive = playerActiveBombs.getOrDefault(playerId, 0);
        playerActiveBombs.put(playerId, Math.max(0, currentActive - 1));

        // Mettre à jour SEULEMENT ce joueur
        updateSinglePlayerHUD(playerId);
    }

    // NOUVELLE MÉTHODE : Mettre à jour un seul joueur
    private void updateSinglePlayerHUD(int playerId) {
        if (controller == null || controller.getGame() == null) return;

        List<Player> players = controller.getGame().getPlayers();
        Player targetPlayer = null;

        for (Player player : players) {
            if (player.getId() == playerId) {
                targetPlayer = player;
                break;
            }
        }

        if (targetPlayer != null) {
            updatePlayerHUDSection(targetPlayer);
        }
    }

    private HBox getPlayerSection(int playerId) {
        switch (playerId) {
            case 1: return player1Info;
            case 2: return player2Info;
            case 3: return player3Info;
            case 4: return player4Info;
            default: return null;
        }
    }

    private String getCacheKey(Cell cell, int x, int y, List<Player> players) {
        StringBuilder key = new StringBuilder();
        key.append(cell.getType().toString());

        try {
            if (cell.getPowerUp() != null) {
                key.append("_POWERUP");
            }
        } catch (Exception e) {
            // Ignorer
        }

        for (Player player : players) {
            if (player.isAlive() && player.getX() == x && player.getY() == y) {
                key.append("_PLAYER").append(player.getId());
                break;
            }
        }

        return key.toString();
    }

    private Image getCachedOrCreateImage(Cell cell, int x, int y, List<Player> players) {
        String cacheKey = getCacheKey(cell, x, y, players);

        Image cachedImage = imageCache.get(cacheKey);
        if (cachedImage != null) {
            return cachedImage;
        }

        Image newImage = getLayeredImageForCell(cell, x, y, players);

        if (imageCache.size() < 100) {
            imageCache.put(cacheKey, newImage);
        }

        return newImage;
    }

    private Image getLayeredImageForCell(Cell cell, int x, int y, List<Player> players) {
        Image result = null;

        switch (cell.getType()) {
            case WALL:
                return wallImage;
            case DESTRUCTIBLE_WALL:
                return destructibleWallImage;
            case EXPLOSION:
                result = emptyImage;
                break;
            default:
                result = emptyImage;
                break;
        }

        if (cell.getType() == CellType.EMPTY) {
            try {
                if (cell.getPowerUp() != null) {
                    result = composeImages(result, powerUpImages[0]);
                }
            } catch (Exception e) {
                // Ignorer
            }
        }

        if (cell.getType() == CellType.BOMB) {
            result = composeImages(result, bombImage);
        }

        if (cell.getType() == CellType.EXPLOSION) {
            result = composeImages(result, explosionImage);
        }

        // MODIFICATION : Joueurs plus gros (ratio 0.9 au lieu de préserver complètement)
        for (Player player : players) {
            if (player.isAlive() && player.getX() == x && player.getY() == y) {
                Direction playerDirection = playerDirections.getOrDefault(player.getId(), Direction.DOWN);
                Image rotatedPlayerImage = getRotatedPlayerImage(player.getId(), playerDirection);
                result = composeImagesWithPlayerScale(result, rotatedPlayerImage);
                break;
            }
        }

        return result != null ? result : emptyImage;
    }

    // NOUVELLE MÉTHODE : Composer avec joueur plus gros
    private Image composeImagesWithPlayerScale(Image backgroundImage, Image playerImage) {
        if (backgroundImage == null) return playerImage;
        if (playerImage == null) return backgroundImage;

        Canvas canvas = new Canvas(Constants.CELL_SIZE, Constants.CELL_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Dessiner le fond
        gc.drawImage(backgroundImage, 0, 0, Constants.CELL_SIZE, Constants.CELL_SIZE);

        // Dessiner le joueur avec un ratio plus gros (90% de la cellule)
        double playerSize = Constants.CELL_SIZE * 0.9;
        double offset = (Constants.CELL_SIZE - playerSize) / 2;
        gc.drawImage(playerImage, offset, offset, playerSize, playerSize);

        return canvas.snapshot(null, null);
    }

    private Image getRotatedPlayerImage(int playerId, Direction direction) {
        if (playerDirectionalImages == null || playerId < 1 || playerId > 4) {
            return playerImages != null && playerId >= 1 && playerId <= 4 ?
                    playerImages[playerId - 1] : null;
        }

        int directionIndex = getDirectionIndex(direction);
        return playerDirectionalImages[playerId - 1][directionIndex];
    }

    private int getDirectionIndex(Direction direction) {
        switch (direction) {
            case UP: return 0;
            case RIGHT: return 1;
            case DOWN: return 2;
            case LEFT: return 3;
            default: return 2;
        }
    }

    public void setPlayerDirection(int playerId, Direction direction) {
        playerDirections.put(playerId, direction);
        clearImageCache();
    }

    public void updateBoard(GameBoard board) {
        if (cellViews == null || board == null) return;

        Cell[][] grid = board.getGrid();
        List<Player> players = controller.getGame().getPlayers();

        for (int x = 0; x < Constants.BOARD_WIDTH; x++) {
            for (int y = 0; y < Constants.BOARD_HEIGHT; y++) {
                ImageView cellView = cellViews[x][y];
                Cell cell = grid[x][y];

                Image composedImage = getLayeredImageForCell(cell, x, y, players);
                cellView.setImage(composedImage);
                adaptRatioForCellType(cellView, cell, x, y, players);
            }
        }

        updateBombermanHUD(players);
    }

    private void initializeGameHUD(int playerCount) {
        hideAllPlayerSections();

        // Réinitialiser les compteurs de bombes
        for (int i = 1; i <= 4; i++) {
            playerActiveBombs.put(i, 0);
            playerMaxBombs.put(i, 1);
        }

        if (player1Info != null && playerCount >= 1) {
            player1Info.setVisible(true);
            player1Info.setManaged(true);
            if (player1Icon != null && playerIconImages != null) {
                player1Icon.setImage(playerIconImages[0]);
                player1Icon.setFitWidth(40);
                player1Icon.setFitHeight(40);
            }
            if (player1BombIcon != null && bombIconImage != null) {
                player1BombIcon.setImage(bombIconImage);
            }
        }
        if (player2Info != null && playerCount >= 2) {
            player2Info.setVisible(true);
            player2Info.setManaged(true);
            if (player2Icon != null && playerIconImages != null) {
                player2Icon.setImage(playerIconImages[1]);
                player2Icon.setFitWidth(40);
                player2Icon.setFitHeight(40);
            }
            if (player2BombIcon != null && bombIconImage != null) {
                player2BombIcon.setImage(bombIconImage);
            }
        }
        if (player3Info != null && playerCount >= 3) {
            player3Info.setVisible(true);
            player3Info.setManaged(true);
            if (player3Icon != null && playerIconImages != null) {
                player3Icon.setImage(playerIconImages[2]);
                player3Icon.setFitWidth(40);
                player3Icon.setFitHeight(40);
            }
            if (player3BombIcon != null && bombIconImage != null) {
                player3BombIcon.setImage(bombIconImage);
            }
        }
        if (player4Info != null && playerCount >= 4) {
            player4Info.setVisible(true);
            player4Info.setManaged(true);
            if (player4Icon != null && playerIconImages != null) {
                player4Icon.setImage(playerIconImages[3]);
                player4Icon.setFitWidth(40);
                player4Icon.setFitHeight(40);
            }
            if (player4BombIcon != null && bombIconImage != null) {
                player4BombIcon.setImage(bombIconImage);
            }
        }

        // Initialiser avec les bonnes valeurs
        if (player1BombCount != null) player1BombCount.setText("1");
        if (player2BombCount != null) player2BombCount.setText("1");
        if (player3BombCount != null) player3BombCount.setText("1");
        if (player4BombCount != null) player4BombCount.setText("1");
    }

    private void animateCounterChange(Label label) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), label);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.3);
        scale.setToY(1.3);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }

    public void updateSinglePlayer(Player player) {
        updatePlayerHUDSection(player);
    }

    private void adaptRatioForCellType(ImageView cellView, Cell cell, int x, int y, List<Player> players) {
        boolean shouldPreserveRatio = false;

        boolean hasPlayer = false;
        for (Player player : players) {
            if (player.isAlive() && player.getX() == x && player.getY() == y) {
                hasPlayer = true;
                break;
            }
        }

        switch (cell.getType()) {
            case WALL:
            case DESTRUCTIBLE_WALL:
                shouldPreserveRatio = false;
                break;
            case BOMB:
            case EXPLOSION:
                shouldPreserveRatio = true;
                break;
            case EMPTY:
                shouldPreserveRatio = hasPlayer;
                try {
                    if (cell.getPowerUp() != null) {
                        shouldPreserveRatio = true;
                    }
                } catch (Exception e) {
                    // Ignorer
                }
                break;
            default:
                shouldPreserveRatio = false;
                break;
        }

        cellView.setPreserveRatio(shouldPreserveRatio);
    }

    public void clearImageCache() {
        imageCache.clear();
    }

    private Image composeImages(Image backgroundImage, Image overlayImage) {
        if (backgroundImage == null) return overlayImage;
        if (overlayImage == null) return backgroundImage;

        Canvas canvas = new Canvas(Constants.CELL_SIZE, Constants.CELL_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.drawImage(backgroundImage, 0, 0, Constants.CELL_SIZE, Constants.CELL_SIZE);
        drawImagePreservingRatio(gc, overlayImage, 0, 0, Constants.CELL_SIZE, Constants.CELL_SIZE);

        return canvas.snapshot(null, null);
    }

    private void drawImagePreservingRatio(GraphicsContext gc, Image image, double x, double y, double width, double height) {
        if (image == null) return;

        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double imageRatio = imageWidth / imageHeight;
        double targetRatio = width / height;

        double drawWidth, drawHeight, drawX, drawY;

        if (imageRatio > targetRatio) {
            drawWidth = width;
            drawHeight = width / imageRatio;
            drawX = x;
            drawY = y + (height - drawHeight) / 2;
        } else {
            drawHeight = height;
            drawWidth = height * imageRatio;
            drawX = x + (width - drawWidth) / 2;
            drawY = y;
        }

        gc.drawImage(image, drawX, drawY, drawWidth, drawHeight);
    }

    private void setupGameGrid() {
        if (gameGrid != null) {
            gameGrid.setAlignment(Pos.CENTER);
            gameGrid.setHgap(0);
            gameGrid.setVgap(0);
            gameGrid.setStyle("-fx-background-color: transparent;");

            cellViews = new ImageView[Constants.BOARD_WIDTH][Constants.BOARD_HEIGHT];

            for (int x = 0; x < Constants.BOARD_WIDTH; x++) {
                for (int y = 0; y < Constants.BOARD_HEIGHT; y++) {
                    ImageView cellView = new ImageView();
                    cellView.setFitWidth(Constants.CELL_SIZE);
                    cellView.setFitHeight(Constants.CELL_SIZE);
                    cellView.setSmooth(true);
                    cellView.setCache(true);
                    cellView.setStyle("-fx-background-color: transparent;");

                    cellViews[x][y] = cellView;
                    gameGrid.add(cellView, x, y);
                }
            }
        }
    }

    private void loadImages() {
        try {
            emptyImage = loadImageOrDefault("images/empty.png", Color.rgb(16, 121, 49));
            wallImage = loadImageOrDefault("images/wall.png", Color.DARKGRAY);
            destructibleWallImage = loadImageOrDefault("images/destructible_wall.png", Color.BROWN);
            bombImage = loadImageOrDefault("images/bomb.gif", Color.BLACK);
            explosionImage = loadImageOrDefault("images/explosion.png", Color.ORANGE);

            loadPlayerDirectionalImages();
            loadPlayerIconImages();
            loadBombIcon(); // NOUVEAU : Charger l'icône de bombe pour le HUD
            createDefaultPowerUpImages();

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des images: " + e.getMessage());
            createDefaultImages();
        }
    }

    private void loadPlayerDirectionalImages() {
        playerDirectionalImages = new Image[4][4];
        Color[] playerColors = {Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW};
        String[] directions = {"upward", "right", "downward", "left"};

        for (int playerId = 0; playerId < 4; playerId++) {
            for (int dirIndex = 0; dirIndex < 4; dirIndex++) {
                String imagePath = String.format("images/player" + playerId + 1 + "_" + directions[dirIndex] + ".gif");

                Image directionalImage = loadImageOrDefault(imagePath, null);

                if (directionalImage == null) {
                    String generalImagePath = String.format("images/player%d.png", playerId + 1);
                    directionalImage = loadImageOrDefault(generalImagePath, null);
                }

                if (directionalImage == null) {
                    directionalImage = createDirectionalPlayerImage(playerColors[playerId], indexToDirection(dirIndex));
                }

                playerDirectionalImages[playerId][dirIndex] = directionalImage;
            }
        }

        playerImages = new Image[4];
        for (int i = 0; i < 4; i++) {
            playerImages[i] = playerDirectionalImages[i][2];
        }
    }

    private void loadPlayerIconImages() {
        playerIconImages = new Image[4];

        for (int i = 0; i < 4; i++) {
            // MODIFICATION : Charger les icônes spécifiques du HUD
            String iconPath = String.format("images/player%d_hud_icon.png", i + 1);
            Image iconImage = loadImageOrDefault(iconPath, null);

            if (iconImage == null) {
                iconPath = String.format("images/player%d_icon.png", i + 1);
                iconImage = loadImageOrDefault(iconPath, null);
            }

            if (iconImage == null && playerImages != null && playerImages[i] != null) {
                iconImage = playerImages[i];
            }

            if (iconImage == null && playerDirectionalImages != null) {
                iconImage = playerDirectionalImages[i][2];
            }

            playerIconImages[i] = iconImage;

            if (iconImage != null) {
                System.out.println("Icône joueur " + (i + 1) + " chargée : " + iconPath);
            } else {
                System.err.println("Impossible de charger l'icône pour le joueur " + (i + 1));
            }
        }
    }

    // NOUVELLE MÉTHODE : Charger l'icône de bombe pour le HUD
    private void loadBombIcon() {
        String bombIconPath = "images/bomb_hud_icon.png";
        bombIconImage = loadImageOrDefault(bombIconPath, null);

        if (bombIconImage == null) {
            // Utiliser l'image de bombe normale si pas d'icône spécifique
            bombIconImage = bombImage;
        }

        if (bombIconImage == null) {
            // Créer une icône de bombe par défaut
            bombIconImage = createBombIcon();
        }

        System.out.println("Icône de bombe chargée pour le HUD");
    }

    // NOUVELLE MÉTHODE : Créer une icône de bombe par défaut
    private Image createBombIcon() {
        Canvas canvas = new Canvas(16, 16);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Fond transparent
        gc.clearRect(0, 0, 16, 16);

        // Corps de la bombe (cercle noir)
        gc.setFill(Color.BLACK);
        gc.fillOval(2, 4, 12, 10);

        // Mèche (petite ligne)
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(2);
        gc.strokeLine(8, 1, 8, 4);

        // Étincelle (petit point rouge)
        gc.setFill(Color.RED);
        gc.fillOval(7, 0, 2, 2);

        return canvas.snapshot(null, null);
    }

    private Image loadImageOrDefault(String path, Color defaultColor) {
        try {
            InputStream imageStream = getClass().getResourceAsStream(path);
            if (imageStream != null) {
                Image image = new Image(imageStream);
                if (!image.isError()) {
                    return image;
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du chargement de l'image: " + path);
        }

        System.out.println("Image non trouvée: " + path + ", utilisation de la forme par défaut");

        if (defaultColor != null) {
            return createColoredImage(defaultColor);
        } else {
            return null;
        }
    }

    private void createDefaultImages() {
        emptyImage = createColoredImage(Color.rgb(16, 121, 49));
        wallImage = createColoredImage(Color.DARKGRAY);
        destructibleWallImage = createColoredImage(Color.BROWN);
        bombImage = createColoredImage(Color.BLACK);
        explosionImage = createColoredImage(Color.ORANGE);

        createDefaultPlayerDirectionalImages();
        createDefaultPowerUpImages();
    }

    private void createDefaultPlayerDirectionalImages() {
        playerDirectionalImages = new Image[4][4];
        Color[] playerColors = {Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW};

        for (int playerId = 0; playerId < 4; playerId++) {
            for (int dirIndex = 0; dirIndex < 4; dirIndex++) {
                playerDirectionalImages[playerId][dirIndex] =
                        createDirectionalPlayerImage(playerColors[playerId], indexToDirection(dirIndex));
            }
        }
    }

    private Image createDirectionalPlayerImage(Color color, Direction direction) {
        Canvas canvas = new Canvas(Constants.CELL_SIZE, Constants.CELL_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.clearRect(0, 0, Constants.CELL_SIZE, Constants.CELL_SIZE);

        double margin = Constants.CELL_SIZE * 0.1;
        double size = Constants.CELL_SIZE - 2 * margin;

        gc.setFill(color);
        gc.fillOval(margin, margin, size, size);

        gc.setFill(Color.WHITE);
        double centerX = Constants.CELL_SIZE / 2;
        double centerY = Constants.CELL_SIZE / 2;
        double arrowSize = Constants.CELL_SIZE * 0.2;

        switch (direction) {
            case UP:
                gc.fillPolygon(
                        new double[]{centerX, centerX - arrowSize/2, centerX + arrowSize/2},
                        new double[]{centerY - arrowSize, centerY, centerY},
                        3
                );
                break;
            case RIGHT:
                gc.fillPolygon(
                        new double[]{centerX + arrowSize, centerX, centerX},
                        new double[]{centerY, centerY - arrowSize/2, centerY + arrowSize/2},
                        3
                );
                break;
            case DOWN:
                gc.fillPolygon(
                        new double[]{centerX, centerX - arrowSize/2, centerX + arrowSize/2},
                        new double[]{centerY + arrowSize, centerY, centerY},
                        3
                );
                break;
            case LEFT:
                gc.fillPolygon(
                        new double[]{centerX - arrowSize, centerX, centerX},
                        new double[]{centerY, centerY - arrowSize/2, centerY + arrowSize/2},
                        3
                );
                break;
        }

        return canvas.snapshot(null, null);
    }

    private Direction indexToDirection(int index) {
        switch (index) {
            case 0: return Direction.UP;
            case 1: return Direction.RIGHT;
            case 2: return Direction.DOWN;
            case 3: return Direction.LEFT;
            default: return Direction.DOWN;
        }
    }

    private void createDefaultPowerUpImages() {
        powerUpImages = new Image[4];
        Color[] powerUpColors = {Color.PURPLE, Color.CYAN, Color.MAGENTA, Color.PINK};
        for (int i = 0; i < powerUpImages.length; i++) {
            powerUpImages[i] = createColoredImage(powerUpColors[i]);
        }
    }

    private Image createColoredImage(Color color) {
        Canvas canvas = new Canvas(Constants.CELL_SIZE, Constants.CELL_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.clearRect(0, 0, Constants.CELL_SIZE, Constants.CELL_SIZE);
        gc.setFill(color);

        if (color == Color.BLUE || color == Color.RED || color == Color.GREEN || color == Color.YELLOW) {
            double margin = Constants.CELL_SIZE * 0.1;
            gc.fillOval(margin, margin,
                    Constants.CELL_SIZE - 2 * margin,
                    Constants.CELL_SIZE - 2 * margin);
        }
        else if (color == Color.BLACK) {
            double margin = Constants.CELL_SIZE * 0.15;
            gc.fillOval(margin, margin,
                    Constants.CELL_SIZE - 2 * margin,
                    Constants.CELL_SIZE - 2 * margin);
            gc.setStroke(Color.DARKGRAY);
            gc.setLineWidth(2);
            gc.strokeOval(margin, margin,
                    Constants.CELL_SIZE - 2 * margin,
                    Constants.CELL_SIZE - 2 * margin);
        }
        else if (color == Color.ORANGE) {
            double margin = Constants.CELL_SIZE * 0.1;
            gc.setLineWidth(Constants.CELL_SIZE * 0.3);
            gc.setStroke(color);
            gc.strokeLine(margin, Constants.CELL_SIZE / 2,
                    Constants.CELL_SIZE - margin, Constants.CELL_SIZE / 2);
            gc.strokeLine(Constants.CELL_SIZE / 2, margin,
                    Constants.CELL_SIZE / 2, Constants.CELL_SIZE - margin);
        }
        else {
            gc.fillRect(0, 0, Constants.CELL_SIZE, Constants.CELL_SIZE);
            if (color == Color.BROWN) {
                gc.setStroke(Color.DARKGRAY);
                gc.setLineWidth(2);
                gc.strokeRect(1, 1, Constants.CELL_SIZE - 2, Constants.CELL_SIZE - 2);
            }
        }

        return canvas.snapshot(null, null);
    }

    private void setupUI() {
        if (gameTimer != null) {
            // MODIFICATION : Police pixelisée pour le timer
            gameTimer.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Courier New', monospace;");
        }

        if (pauseMenu != null) {
            pauseMenu.setVisible(false);
            pauseMenu.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-padding: 20px;");
        }

        if (gameOverScreen != null) {
            gameOverScreen.setVisible(false);
            gameOverScreen.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-padding: 20px;");
        }

        if (mainMenu != null) {
            mainMenu.setVisible(true);
            mainMenu.setStyle("-fx-background-color: #2c3e50; -fx-padding: 30px;");
        }
    }

    private Image getImageForCell(Cell cell, int x, int y, List<Player> players) {
        Image baseImage = null;
        Image overlayImage = null;

        switch (cell.getType()) {
            case WALL:
                return wallImage;
            case DESTRUCTIBLE_WALL:
                return destructibleWallImage;
            case EXPLOSION:
                return explosionImage;
            case BOMB:
                baseImage = emptyImage;
                overlayImage = bombImage;
                break;
            case EMPTY:
            default:
                baseImage = emptyImage;
                break;
        }

        if (cell.getType() == CellType.EMPTY) {
            try {
                if (cell.getPowerUp() != null) {
                    overlayImage = powerUpImages[0];
                }
            } catch (Exception e) {
                // Ignorer
            }
        }

        for (Player player : players) {
            if (player.isAlive() && player.getX() == x && player.getY() == y) {
                overlayImage = playerImages[player.getId() - 1];
                break;
            }
        }

        if (overlayImage != null) {
            return composeImages(baseImage, overlayImage);
        } else {
            return baseImage != null ? baseImage : emptyImage;
        }
    }

    public void updatePlayerInfo(List<Player> players) {
        if (playerInfo != null) {
            playerInfo.getChildren().clear();
            for (Player player : players) {
                VBox playerBox = createPlayerInfoBox(player);
                playerInfo.getChildren().add(playerBox);
            }
        }

        updateBombermanHUD(players);
    }

    private void updateBombermanHUD(List<Player> players) {
        if (players == null) return;

        for (Player player : players) {
            updatePlayerHUDSection(player);
        }
    }

    private void updatePlayerHUDSection(Player player) {
        int playerId = player.getId();
        HBox playerSection = null;
        Label bombCountLabel = null;

        switch (playerId) {
            case 1:
                playerSection = player1Info;
                bombCountLabel = player1BombCount;
                break;
            case 2:
                playerSection = player2Info;
                bombCountLabel = player2BombCount;
                break;
            case 3:
                playerSection = player3Info;
                bombCountLabel = player3BombCount;
                break;
            case 4:
                playerSection = player4Info;
                bombCountLabel = player4BombCount;
                break;
            default:
                return;
        }

        if (playerSection != null && bombCountLabel != null) {
            playerSection.setVisible(true);
            playerSection.setManaged(true);

            // CORRECTION : Calculer les bombes disponibles correctement
            int availableBombs = getPlayerAvailableBombs(player);

            String newCount = String.valueOf(availableBombs);
            if (!bombCountLabel.getText().equals(newCount)) {
                bombCountLabel.setText(newCount);
                animateCounterChange(bombCountLabel);
            }

            if (!player.isAlive()) {
                playerSection.setOpacity(0.3);
            } else {
                playerSection.setOpacity(1.0);
            }
        }
    }

    private int getPlayerAvailableBombs(Player player) {
        try {
            int playerId = player.getId();

            // Obtenir le maximum de bombes du joueur
            int maxBombs = player.getBombCount();
            playerMaxBombs.put(playerId, maxBombs);

            // Obtenir les bombes actives
            int activeBombs = playerActiveBombs.getOrDefault(playerId, 0);

            // Bombes disponibles = max - actives
            int available = Math.max(0, maxBombs - activeBombs);


            return available;

        } catch (Exception e) {
            System.err.println("Erreur getBombCount pour joueur " + player.getId() + ": " + e.getMessage());
            return 1;
        }
    }

    private void hideAllPlayerSections() {
        if (player1Info != null) player1Info.setVisible(false);
        if (player2Info != null) player2Info.setVisible(false);
        if (player3Info != null) player3Info.setVisible(false);
        if (player4Info != null) player4Info.setVisible(false);
    }

    private VBox createPlayerInfoBox(Player player) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-border-color: " + getPlayerColor(player.getId()) + "; " +
                "-fx-border-width: 2px; -fx-padding: 10px; -fx-background-color: rgba(255,255,255,0.1);");

        Label nameLabel = new Label(player.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

        Label livesLabel = new Label("Vies: " + player.getLives());
        livesLabel.setStyle("-fx-text-fill: white;");

        Label bombsLabel = new Label("Bombes: " + player.getBombCount());
        bombsLabel.setStyle("-fx-text-fill: white;");

        Label rangeLabel = new Label("Portée: " + player.getBombRange());
        rangeLabel.setStyle("-fx-text-fill: white;");

        box.getChildren().addAll(nameLabel, livesLabel, bombsLabel, rangeLabel);

        if (!player.isAlive()) {
            box.setOpacity(0.5);
        }

        return box;
    }

    private String getPlayerColor(int playerId) {
        String[] colors = {"#3498db", "#e74c3c", "#2ecc71", "#f39c12"};
        return colors[(playerId - 1) % colors.length];
    }

    public void updateGameTimer(String timeString) {
        if (gameTimer != null) {
            // MODIFICATION : Format timer style Bomberman (ex: "1:41")
            String formattedTime = timeString.replace("Temps: ", "");

            // Assurer le format minutes:secondes
            if (formattedTime.startsWith("00:")) {
                formattedTime = formattedTime.substring(1); // Enlever le premier 0
            }

            gameTimer.setText(formattedTime);
        }
    }

    public void showExplosion(int x, int y) {
        if (cellViews != null && x >= 0 && x < Constants.BOARD_WIDTH &&
                y >= 0 && y < Constants.BOARD_HEIGHT) {

            ImageView cellView = cellViews[x][y];

            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), cellView);
            scaleTransition.setFromX(1.0);
            scaleTransition.setFromY(1.0);
            scaleTransition.setToX(1.3);
            scaleTransition.setToY(1.3);
            scaleTransition.setAutoReverse(true);
            scaleTransition.setCycleCount(2);
            scaleTransition.play();
        }
    }

    public void showPauseMenu() {
        if (pauseMenu != null) {
            pauseMenu.setVisible(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), pauseMenu);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

    public void hidePauseMenu() {
        if (pauseMenu != null) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), pauseMenu);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> pauseMenu.setVisible(false));
            fadeOut.play();
        }
    }

    public void showGameOver(Player winner) {
        if (gameOverScreen != null && winnerLabel != null) {
            String message;
            if (winner != null) {
                message = winner.getName() + " a gagné !";
            } else {
                message = "Égalité !";
            }

            winnerLabel.setText(message);
            gameOverScreen.setVisible(true);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), gameOverScreen);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

    public void showMainMenu() {
        hideGameInterface();

        if (mainMenu != null) {
            mainMenu.setVisible(true);
        }

        if (gameOverScreen != null) {
            gameOverScreen.setVisible(false);
        }

        if (pauseMenu != null) {
            pauseMenu.setVisible(false);
        }
    }

    @FXML
    private void onShowControls() {
        hideMainMenu();
        showControlsScreen();
    }

    @FXML
    private void onResumeGame() {
        if (controller != null) {
            controller.pauseGame();
            hidePauseMenu();
        }
    }

    @FXML
    private void onRestartGame() {
        hidePauseMenu();
        hideGameInterface();
        if (controller != null) {
            controller.startNewGame();
            showGameInterface();
        }
    }

    @FXML
    private void onReturnToMenu() {
        if (controller != null) {
            controller.returnToMenu();
        }
        hidePauseMenu();
        hideGameInterface();
        showMainMenu();
    }

    @FXML
    private void onQuitGame() {
        System.exit(0);
    }

    private void hideMainMenu() {
        if (mainMenu != null) {
            mainMenu.setVisible(false);
        }
    }
}