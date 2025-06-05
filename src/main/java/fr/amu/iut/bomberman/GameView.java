package fr.amu.iut.bomberman;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
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

    private ImageView[][] cellViews;
    private GameController controller;

    // Images pour les différents éléments
    private Image emptyImage;
    private Image wallImage;
    private Image destructibleWallImage;
    private Image bombImage;
    private Image explosionImage;
    private Image[] playerImages;
    private Image[] powerUpImages;

    private Map<String, Image> imageCache = new HashMap<>();

    public void initialize() {
        setupGameGrid();
        loadImages();
        setupUI();
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }

    @FXML
    private void onStartGame3Players() {
        if (controller != null) {
            controller.startNewGame(3);
            hideMainMenu();
            showGameInterface();
        }
    }

    @FXML
    private void onStartGame4Players() {
        if (controller != null) {
            controller.startNewGame(4);
            hideMainMenu();
            showGameInterface();
        }
    }

    @FXML
    private void onShowControls() {
        hideMainMenu();
        showControlsScreen();
    }

    @FXML
    private void onHideControls() {
        hideControlsScreen();
        showMainMenu();
    }

    // Méthodes utilitaires pour gérer l'affichage des écrans
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

    private String getCacheKey(Cell cell, int x, int y, List<Player> players) {
        StringBuilder key = new StringBuilder();
        key.append(cell.getType().toString());

        // Ajouter power-up si présent
        try {
            if (cell.getPowerUp() != null) {
                key.append("_POWERUP");
            }
        } catch (Exception e) {
            // Ignorer
        }

        // Ajouter joueur si présent
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

        // Créer la nouvelle image composée
        Image newImage = getLayeredImageForCell(cell, x, y, players);

        // Mettre en cache (attention à ne pas trop remplir le cache)
        if (imageCache.size() < 100) { // Limite pour éviter les fuites mémoire
            imageCache.put(cacheKey, newImage);
        }

        return newImage;
    }

    private Image getLayeredImageForCell(Cell cell, int x, int y, List<Player> players) {
        Image result = null;

        // Couche 1 : Fond de base (toujours présent)
        switch (cell.getType()) {
            case WALL:
                return wallImage; // Les murs restent sans superposition
            case DESTRUCTIBLE_WALL:
                return destructibleWallImage; // Les murs destructibles aussi
            case EXPLOSION:
                result = emptyImage; // Fond vide pour les explosions aussi
                break;
            default:
                result = emptyImage; // Fond vide
                break;
        }

        // Couche 2 : Power-ups (si présents)
        if (cell.getType() == CellType.EMPTY) {
            try {
                if (cell.getPowerUp() != null) {
                    result = composeImages(result, powerUpImages[0]);
                }
            } catch (Exception e) {
                // Ignorer si getPowerUp() n'existe pas
            }
        }

        // Couche 3 : Bombes
        if (cell.getType() == CellType.BOMB) {
            result = composeImages(result, bombImage);
        }

        // Couche 4 : Explosions (par-dessus le fond)
        if (cell.getType() == CellType.EXPLOSION) {
            result = composeImages(result, explosionImage);
        }

        // Couche 5 : Joueurs (au-dessus de tout, même des explosions)
        for (Player player : players) {
            if (player.isAlive() && player.getX() == x && player.getY() == y) {
                result = composeImages(result, playerImages[player.getId() - 1]);
                break;
            }
        }

        return result != null ? result : emptyImage;
    }

    public void updateBoard(GameBoard board) {
        if (cellViews == null || board == null) return;

        Cell[][] grid = board.getGrid();
        List<Player> players = controller.getGame().getPlayers();

        // Mettre à jour chaque cellule avec superposition
        for (int x = 0; x < Constants.BOARD_WIDTH; x++) {
            for (int y = 0; y < Constants.BOARD_HEIGHT; y++) {
                ImageView cellView = cellViews[x][y];
                Cell cell = grid[x][y];

                // Utiliser la méthode avec superposition
                Image composedImage = getLayeredImageForCell(cell, x, y, players);
                cellView.setImage(composedImage);
            }
        }
    }

    public void clearImageCache() {
        imageCache.clear();
    }

    private Image composeImages(Image backgroundImage, Image overlayImage) {
        if (backgroundImage == null) return overlayImage;
        if (overlayImage == null) return backgroundImage;

        // Créer un canvas de la taille d'une cellule
        Canvas canvas = new Canvas(Constants.CELL_SIZE, Constants.CELL_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Dessiner l'image de fond d'abord
        gc.drawImage(backgroundImage, 0, 0, Constants.CELL_SIZE, Constants.CELL_SIZE);

        // Dessiner l'image de superposition par-dessus
        gc.drawImage(overlayImage, 0, 0, Constants.CELL_SIZE, Constants.CELL_SIZE);

        // Retourner l'image composée
        return canvas.snapshot(null, null);
    }

    private void setupGameGrid() {
        if (gameGrid != null) {
            // Configurer la grille de jeu
            gameGrid.setAlignment(Pos.CENTER);
            gameGrid.setHgap(1);
            gameGrid.setVgap(1);
            gameGrid.setStyle("-fx-background-color: #333333;");

            // Initialiser la matrice de vues
            cellViews = new ImageView[Constants.BOARD_WIDTH][Constants.BOARD_HEIGHT];

            for (int x = 0; x < Constants.BOARD_WIDTH; x++) {
                for (int y = 0; y < Constants.BOARD_HEIGHT; y++) {
                    ImageView cellView = new ImageView();
                    cellView.setFitWidth(Constants.CELL_SIZE);
                    cellView.setFitHeight(Constants.CELL_SIZE);
                    cellView.setPreserveRatio(false);

                    // Améliorer le rendu des images
                    cellView.setSmooth(true);
                    cellView.setCache(true);

                    // Définir un arrière-plan transparent par défaut
                    cellView.setStyle("-fx-background-color: transparent;");

                    cellViews[x][y] = cellView;
                    gameGrid.add(cellView, x, y);
                }
            }
        }
    }

    private void loadImages() {
        try {
            // Chargement des images depuis les ressources
            emptyImage = loadImageOrDefault("images/empty.png", Color.LIGHTGRAY);
            wallImage = loadImageOrDefault("images/wall.png", Color.DARKGRAY);
            destructibleWallImage = loadImageOrDefault("images/destructible_wall.jpg", Color.BROWN);
            bombImage = loadImageOrDefault("images/bomb.png", Color.BLACK);
            explosionImage = loadImageOrDefault("images/explosion.png", Color.ORANGE);

            // Images des joueurs
            playerImages = new Image[4];
            Color[] playerColors = {Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW};
            for (int i = 0; i < 4; i++) {
                playerImages[i] = loadImageOrDefault("images/player" + (i + 1) + ".png", playerColors[i]);
            }

            // Images des power-ups - création par défaut car PowerUpType peut ne pas exister
            createDefaultPowerUpImages();

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des images: " + e.getMessage());
            createDefaultImages();
        }
    }

    // 2. Améliorer le chargement des images pour préserver la transparence
    private Image loadImageOrDefault(String path, Color defaultColor) {
        try {
            InputStream imageStream = getClass().getResourceAsStream(path);
            if (imageStream != null) {
                Image image = new Image(imageStream);
                // Vérifier si l'image s'est chargée correctement
                if (!image.isError()) {
                    return image;
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du chargement de l'image: " + path);
        }

        System.out.println("Image non trouvée: " + path + ", utilisation de la forme par défaut");
        return createColoredImage(defaultColor);
    }

    private void createDefaultImages() {
        // Créer des images par défaut si les fichiers ne sont pas trouvés
        emptyImage = createColoredImage(Color.LIGHTGRAY);
        wallImage = createColoredImage(Color.DARKGRAY);
        destructibleWallImage = createColoredImage(Color.BROWN);
        bombImage = createColoredImage(Color.BLACK);
        explosionImage = createColoredImage(Color.ORANGE);

        playerImages = new Image[4];
        Color[] playerColors = {Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW};
        for (int i = 0; i < 4; i++) {
            playerImages[i] = createColoredImage(playerColors[i]);
        }

        createDefaultPowerUpImages();
    }

    private void createDefaultPowerUpImages() {
        // Créer des images par défaut pour les power-ups
        powerUpImages = new Image[4]; // Nombre arbitraire de power-ups
        Color[] powerUpColors = {Color.PURPLE, Color.CYAN, Color.MAGENTA, Color.PINK};
        for (int i = 0; i < powerUpImages.length; i++) {
            powerUpImages[i] = createColoredImage(powerUpColors[i]);
        }
    }

    private Image createColoredImage(Color color) {
        // Créer un Canvas pour dessiner une forme avec transparence
        Canvas canvas = new Canvas(Constants.CELL_SIZE, Constants.CELL_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Effacer le canvas (transparent par défaut)
        gc.clearRect(0, 0, Constants.CELL_SIZE, Constants.CELL_SIZE);

        // Dessiner un cercle ou une forme au lieu d'un rectangle plein
        gc.setFill(color);

        // Pour les joueurs : dessiner un cercle
        if (color == Color.BLUE || color == Color.RED || color == Color.GREEN || color == Color.YELLOW) {
            double margin = Constants.CELL_SIZE * 0.1; // 10% de marge
            gc.fillOval(margin, margin,
                    Constants.CELL_SIZE - 2 * margin,
                    Constants.CELL_SIZE - 2 * margin);
        }
        // Pour les bombes : dessiner un cercle noir avec contour
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
        // Pour les explosions : dessiner une croix
        else if (color == Color.ORANGE) {
            double margin = Constants.CELL_SIZE * 0.1;
            gc.setLineWidth(Constants.CELL_SIZE * 0.3);
            gc.setStroke(color);
            // Croix horizontale
            gc.strokeLine(margin, Constants.CELL_SIZE / 2,
                    Constants.CELL_SIZE - margin, Constants.CELL_SIZE / 2);
            // Croix verticale
            gc.strokeLine(Constants.CELL_SIZE / 2, margin,
                    Constants.CELL_SIZE / 2, Constants.CELL_SIZE - margin);
        }
        // Pour les murs : garder le rectangle mais avec des bords
        else {
            gc.fillRect(0, 0, Constants.CELL_SIZE, Constants.CELL_SIZE);
            if (color == Color.BROWN) { // Mur destructible
                gc.setStroke(Color.DARKGRAY);
                gc.setLineWidth(2);
                gc.strokeRect(1, 1, Constants.CELL_SIZE - 2, Constants.CELL_SIZE - 2);
            }
        }

        // Convertir le canvas en image
        return canvas.snapshot(null, null);
    }

    private void setupUI() {
        // Configuration des éléments UI
        if (gameTimer != null) {
            gameTimer.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
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

        // 1. Déterminer l'image de base selon le type de cellule
        switch (cell.getType()) {
            case WALL:
                return wallImage; // Les murs ne peuvent pas avoir de superposition
            case DESTRUCTIBLE_WALL:
                return destructibleWallImage; // Les murs destructibles non plus
            case EXPLOSION:
                return explosionImage; // Les explosions remplacent tout
            case BOMB:
                baseImage = emptyImage; // Fond vide
                overlayImage = bombImage; // Bombe par-dessus
                break;
            case EMPTY:
            default:
                baseImage = emptyImage; // Fond vide par défaut
                break;
        }

        // 2. Vérifier s'il y a un power-up sur cette case
        if (cell.getType() == CellType.EMPTY) {
            try {
                if (cell.getPowerUp() != null) {
                    overlayImage = powerUpImages[0]; // Power-up par-dessus le fond
                }
            } catch (Exception e) {
                // La méthode getPowerUp() n'existe peut-être pas
            }
        }

        // 3. Vérifier s'il y a un joueur sur cette case (priorité la plus haute)
        for (Player player : players) {
            if (player.isAlive() && player.getX() == x && player.getY() == y) {
                overlayImage = playerImages[player.getId() - 1]; // Joueur par-dessus tout
                break;
            }
        }

        // 4. Composer l'image finale
        if (overlayImage != null) {
            return composeImages(baseImage, overlayImage);
        } else {
            return baseImage != null ? baseImage : emptyImage;
        }
    }

    public void updatePlayerInfo(List<Player> players) {
        if (playerInfo == null) return;

        playerInfo.getChildren().clear();

        for (Player player : players) {
            VBox playerBox = createPlayerInfoBox(player);
            playerInfo.getChildren().add(playerBox);
        }
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

        // Griser si le joueur est mort
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
            gameTimer.setText("Temps: " + timeString);
        }
    }

    public void showExplosion(int x, int y) {
        if (cellViews != null && x >= 0 && x < Constants.BOARD_WIDTH &&
                y >= 0 && y < Constants.BOARD_HEIGHT) {

            ImageView cellView = cellViews[x][y];

            // Animation d'explosion
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
        hideGameInterface(); // Ajout de cette ligne importante

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
    private void onStartGame() {
        if (controller != null) {
            controller.startNewGame(2); // 2 joueurs par défaut
            hideMainMenu();
            showGameInterface();
        }
    }

    @FXML
    private void onStartGameWithPlayers(int playerCount) {
        if (controller != null) {
            controller.startNewGame(playerCount);
            hideMainMenu();
            showGameInterface(); // Ajout de cette ligne importante
        }
    }

    @FXML
    private void onResumeGame() {
        if (controller != null) {
            controller.pauseGame();
        }
    }

    @FXML
    private void onRestartGame() {
        hidePauseMenu();
        hideGameInterface();
        if (controller != null) {
            controller.startNewGame();
            showGameInterface(); // Afficher l'interface après restart
        }
    }

    @FXML
    private void onReturnToMenu() {
        if (controller != null) {
            controller.returnToMenu();
        }
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