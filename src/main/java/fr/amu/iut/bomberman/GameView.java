package fr.amu.iut.bomberman;

import javafx.animation.FadeTransition;
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

import java.util.List;
import java.util.Objects;

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

    private Image loadImageOrDefault(String path, Color defaultColor) {
        try {
            return new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
        } catch (Exception e) {
            System.out.println("Image non trouvée: " + path + ", utilisation de la couleur par défaut");
            return createColoredImage(defaultColor);
        }
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
        // Créer une image simple avec une couleur unie
        Rectangle rect = new Rectangle(Constants.CELL_SIZE, Constants.CELL_SIZE, color);
        return rect.snapshot(null, null);
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

    public void updateBoard(GameBoard board) {
        if (cellViews == null || board == null) return;

        Cell[][] grid = board.getGrid();
        List<Player> players = controller.getGame().getPlayers();

        // Mettre à jour chaque cellule
        for (int x = 0; x < Constants.BOARD_WIDTH; x++) {
            for (int y = 0; y < Constants.BOARD_HEIGHT; y++) {
                ImageView cellView = cellViews[x][y];
                Cell cell = grid[x][y];

                // Déterminer quelle image afficher
                Image imageToShow = getImageForCell(cell, x, y, players);
                cellView.setImage(imageToShow);
            }
        }
    }

    private Image getImageForCell(Cell cell, int x, int y, List<Player> players) {
        // Vérifier s'il y a un joueur sur cette case
        for (Player player : players) {
            if (player.isAlive() && player.getX() == x && player.getY() == y) {
                return playerImages[player.getId() - 1];
            }
        }

        // Vérifier le type de cellule
        return switch (cell.getType()) {
            case WALL -> wallImage;
            case DESTRUCTIBLE_WALL -> destructibleWallImage;
            case BOMB -> bombImage;
            case EXPLOSION -> explosionImage;
            default -> {
                // Vérifier s'il y a un power-up (seulement si getPowerUp() existe)
                try {
                    if (cell.getPowerUp() != null) {
                        // Utiliser une image de power-up par défaut
                        yield powerUpImages[0];
                    }
                } catch (Exception e) {
                    // La méthode getPowerUp() n'existe peut-être pas
                }
                yield emptyImage;
            }
        };
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
            fadeOut.setOnFinished(_ -> pauseMenu.setVisible(false));
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
    private void onResumeGame() {
        if (controller != null) {
            controller.pauseGame();
        }
    }

    @FXML
    private void onRestartGame() {
        hideGameInterface();
        if (controller != null) {
            controller.startNewGame();
            showGameInterface(); // Afficher l'interface après restart
            hidePauseMenu();

            // Hide game over screen as well when restarting
            if (gameOverScreen != null) {
                gameOverScreen.setVisible(false);
            }
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