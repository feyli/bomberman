package fr.amu.iut.bomberman.controller;

import fr.amu.iut.bomberman.model.PlayerProfile;
import fr.amu.iut.bomberman.utils.ProfileManager;
import fr.amu.iut.bomberman.utils.SoundManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;

/**
 * Contrôleur pour la sélection des joueurs
 * Permet de choisir les profils avant de commencer une partie
 * Support pour les bots ajouté
 *
 * @author Super Bomberman Team
 * @version 1.0
 */
public class PlayerSelectionController {

    @FXML
    private ComboBox<PlayerProfile> player1ComboBox;
    @FXML
    private ComboBox<PlayerProfile> player2ComboBox;
    @FXML
    private ImageView player1Avatar;
    @FXML
    private ImageView player2Avatar;
    @FXML
    private Spinner<Integer> roundsSpinner;
    @FXML
    private ComboBox<String> timeComboBox;
    @FXML
    private Button startButton;

    // NOUVEAUX ÉLÉMENTS POUR LES BOTS
    @FXML
    private CheckBox player1BotCheckbox;
    @FXML
    private CheckBox player2BotCheckbox;
    @FXML
    private ComboBox<String> gameModeComboBox;
    @FXML
    private Label player1Label;
    @FXML
    private Label player2Label;

    private ProfileManager profileManager;
    private ObservableList<PlayerProfile> profiles;

    // Profils de bots prédéfinis
    private PlayerProfile botEasy;
    private PlayerProfile botMedium;
    private PlayerProfile botHard;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        System.out.println("PlayerSelectionController initialisé");

        profileManager = ProfileManager.getInstance();
        loadProfiles();

        // Créer les profils de bots
        createBotProfiles();

        // Configuration des ComboBox
        setupComboBoxes();

        // Configuration du mode de jeu
        setupGameModeComboBox();

        // Configuration de la ComboBox de temps
        setupTimeComboBox();

        // Listeners pour activer/désactiver le bouton Start
        player1ComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updatePlayer1Display(newVal);
            checkStartButtonState();
        });

        player2ComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updatePlayer2Display(newVal);
            checkStartButtonState();
        });

        // Listeners pour les checkboxes de bots
        if (player1BotCheckbox != null) {
            player1BotCheckbox.setOnAction(e -> handlePlayer1BotChange());
        }
        if (player2BotCheckbox != null) {
            player2BotCheckbox.setOnAction(e -> handlePlayer2BotChange());
        }

        // Listener pour le mode de jeu
        if (gameModeComboBox != null) {
            gameModeComboBox.setOnAction(e -> handleGameModeChange());
        }

        // Configuration du spinner
        if (roundsSpinner != null) {
            SpinnerValueFactory<Integer> valueFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9, 3);
            roundsSpinner.setValueFactory(valueFactory);
        }

        // Jouer un son d'entrée
        SoundManager.getInstance().playSound("menu_select");
    }

    /**
     * Crée les profils de bots prédéfinis
     */
    private void createBotProfiles() {
        botEasy = new PlayerProfile("Robot", "Facile", "Bot Easy");
        botMedium = new PlayerProfile("Robot", "Moyen", "Bot Medium");
        botHard = new PlayerProfile("Robot", "Difficile", "Bot Hard");
    }

    /**
     * Configure la ComboBox de mode de jeu
     */
    private void setupGameModeComboBox() {
        if (gameModeComboBox != null) {
            gameModeComboBox.setItems(FXCollections.observableArrayList(
                    "Joueur vs Joueur",
                    "Joueur vs Bot",
                    "Bot vs Bot"
            ));
            gameModeComboBox.setValue("Joueur vs Joueur");
        }
    }

    /**
     * Gère le changement de mode de jeu
     */
    private void handleGameModeChange() {
        if (gameModeComboBox == null) return;

        String selectedMode = gameModeComboBox.getValue();

        switch (selectedMode) {
            case "Joueur vs Joueur":
                if (player1BotCheckbox != null) player1BotCheckbox.setSelected(false);
                if (player2BotCheckbox != null) player2BotCheckbox.setSelected(false);
                break;

            case "Joueur vs Bot":
                if (player1BotCheckbox != null) player1BotCheckbox.setSelected(false);
                if (player2BotCheckbox != null) player2BotCheckbox.setSelected(true);
                break;

            case "Bot vs Bot":
                if (player1BotCheckbox != null) player1BotCheckbox.setSelected(true);
                if (player2BotCheckbox != null) player2BotCheckbox.setSelected(true);
                break;
        }

        handlePlayer1BotChange();
        handlePlayer2BotChange();
    }

    /**
     * Gère le changement de statut bot pour le joueur 1
     */
    private void handlePlayer1BotChange() {
        if (player1BotCheckbox == null) return;

        boolean isBot = player1BotCheckbox.isSelected();

        if (isBot) {
            // Mode bot : afficher les bots disponibles
            ObservableList<PlayerProfile> botProfiles = FXCollections.observableArrayList();
            botProfiles.addAll(botEasy, botMedium, botHard);
            player1ComboBox.setItems(botProfiles);
            player1ComboBox.setValue(botMedium);

            if (player1Label != null) {
                player1Label.setText("Robot 1");
            }
        } else {
            // Mode humain : afficher les profils normaux
            player1ComboBox.setItems(profiles);
            if (profiles.size() > 0) {
                player1ComboBox.setValue(profiles.get(0));
            }

            if (player1Label != null) {
                player1Label.setText("Joueur 1");
            }
        }

        updateGameModeDisplay();
        checkStartButtonState();
    }

    /**
     * Gère le changement de statut bot pour le joueur 2
     */
    private void handlePlayer2BotChange() {
        if (player2BotCheckbox == null) return;

        boolean isBot = player2BotCheckbox.isSelected();

        if (isBot) {
            // Mode bot : afficher les bots disponibles
            ObservableList<PlayerProfile> botProfiles = FXCollections.observableArrayList();
            botProfiles.addAll(botEasy, botMedium, botHard);
            player2ComboBox.setItems(botProfiles);
            player2ComboBox.setValue(botHard);

            if (player2Label != null) {
                player2Label.setText("Robot 2");
            }
        } else {
            // Mode humain : afficher les profils normaux
            player2ComboBox.setItems(profiles);
            if (profiles.size() > 1) {
                player2ComboBox.setValue(profiles.get(1));
            } else if (profiles.size() > 0) {
                player2ComboBox.setValue(profiles.get(0));
            }

            if (player2Label != null) {
                player2Label.setText("Joueur 2");
            }
        }

        updateGameModeDisplay();
        checkStartButtonState();
    }

    /**
     * Met à jour l'affichage du mode de jeu selon les sélections
     */
    private void updateGameModeDisplay() {
        if (gameModeComboBox == null || player1BotCheckbox == null || player2BotCheckbox == null) {
            return;
        }

        boolean p1Bot = player1BotCheckbox.isSelected();
        boolean p2Bot = player2BotCheckbox.isSelected();

        if (!p1Bot && !p2Bot) {
            gameModeComboBox.setValue("Joueur vs Joueur");
        } else if (!p1Bot && p2Bot) {
            gameModeComboBox.setValue("Joueur vs Bot");
        } else if (p1Bot && p2Bot) {
            gameModeComboBox.setValue("Bot vs Bot");
        }
    }

    /**
     * Charge les profils disponibles
     */
    private void loadProfiles() {
        profiles = FXCollections.observableArrayList(profileManager.getAllProfiles());

        // Ajouter des profils par défaut si nécessaire
        if (profiles.isEmpty()) {
            profiles.add(new PlayerProfile("Joueur", "1", "Player1"));
            profiles.add(new PlayerProfile("Joueur", "2", "Player2"));

            // Sauvegarder les profils par défaut
            for (PlayerProfile profile : profiles) {
                profileManager.addProfile(profile);
            }
        }
    }

    /**
     * Configure les ComboBox avec les profils
     */
    private void setupComboBoxes() {
        // Configuration de l'affichage des profils
        player1ComboBox.setItems(profiles);
        player2ComboBox.setItems(profiles);

        // Convertisseur pour afficher le nom du profil
        ProfileStringConverter converter = new ProfileStringConverter();
        player1ComboBox.setConverter(converter);
        player2ComboBox.setConverter(converter);

        // Sélection par défaut
        if (profiles.size() > 0) {
            player1ComboBox.setValue(profiles.get(0));
        }
        if (profiles.size() > 1) {
            player2ComboBox.setValue(profiles.get(1));
        } else if (profiles.size() > 0) {
            player2ComboBox.setValue(profiles.get(0));
        }
    }

    /**
     * Configure la ComboBox de temps
     */
    private void setupTimeComboBox() {
        if (timeComboBox != null) {
            timeComboBox.setItems(FXCollections.observableArrayList(
                    "1:30", "2:00", "3:00", "4:00", "5:00"
            ));
            timeComboBox.setValue("3:00");
        }
    }

    /**
     * Met à jour l'affichage du joueur 1
     */
    private void updatePlayer1Display(PlayerProfile profile) {
        if (profile != null && player1Avatar != null) {
            try {
                String avatarPath = profile.getAvatarPath();
                // Chemin spécial pour les bots
                if (profile == botEasy || profile == botMedium || profile == botHard) {
                    avatarPath = "/images/avatars/bot.png";
                }

                Image avatar = new Image(getClass().getResourceAsStream(avatarPath));
                player1Avatar.setImage(avatar);
            } catch (Exception e) {
                System.err.println("Impossible de charger l'avatar: " + profile.getAvatarPath());
                try {
                    Image defaultAvatar = new Image(getClass().getResourceAsStream("/images/avatars/default.png"));
                    player1Avatar.setImage(defaultAvatar);
                } catch (Exception ex) {
                    System.err.println("Impossible de charger l'avatar par défaut");
                }
            }
        }
    }

    /**
     * Met à jour l'affichage du joueur 2
     */
    private void updatePlayer2Display(PlayerProfile profile) {
        if (profile != null && player2Avatar != null) {
            try {
                String avatarPath = profile.getAvatarPath();
                // Chemin spécial pour les bots
                if (profile == botEasy || profile == botMedium || profile == botHard) {
                    avatarPath = "/images/avatars/bot.png";
                }

                Image avatar = new Image(getClass().getResourceAsStream(avatarPath));
                player2Avatar.setImage(avatar);
            } catch (Exception e) {
                System.err.println("Impossible de charger l'avatar: " + profile.getAvatarPath());
                try {
                    Image defaultAvatar = new Image(getClass().getResourceAsStream("/images/avatars/default.png"));
                    player2Avatar.setImage(defaultAvatar);
                } catch (Exception ex) {
                    System.err.println("Impossible de charger l'avatar par défaut");
                }
            }
        }
    }

    /**
     * Vérifie si le bouton Start doit être activé
     */
    private void checkStartButtonState() {
        PlayerProfile p1 = player1ComboBox.getValue();
        PlayerProfile p2 = player2ComboBox.getValue();

        // Pour les bots, on peut avoir le même profil
        boolean p1IsBot = player1BotCheckbox != null && player1BotCheckbox.isSelected();
        boolean p2IsBot = player2BotCheckbox != null && player2BotCheckbox.isSelected();

        boolean canStart = p1 != null && p2 != null;

        // Si les deux sont des joueurs humains, ils doivent être différents
        if (!p1IsBot && !p2IsBot) {
            canStart = canStart && !p1.equals(p2);
        }

        startButton.setDisable(!canStart);

        if (p1 != null && p2 != null && p1.equals(p2) && !p1IsBot && !p2IsBot) {
            System.out.println("Même profil sélectionné pour les deux joueurs humains");
        }
    }

    /**
     * Gère le clic sur "Nouveau Profil" pour le joueur 1
     */
    @FXML
    private void handleNewProfile1() {
        // Seulement si ce n'est pas un bot
        if (player1BotCheckbox != null && player1BotCheckbox.isSelected()) {
            return;
        }

        SoundManager.getInstance().playSound("menu_select");
        createNewProfile(1);
    }

    /**
     * Gère le clic sur "Nouveau Profil" pour le joueur 2
     */
    @FXML
    private void handleNewProfile2() {
        // Seulement si ce n'est pas un bot
        if (player2BotCheckbox != null && player2BotCheckbox.isSelected()) {
            return;
        }

        SoundManager.getInstance().playSound("menu_select");
        createNewProfile(2);
    }

    /**
     * Crée un nouveau profil
     */
    private void createNewProfile(int playerNumber) {
        // Dialogue de création de profil
        Dialog<PlayerProfile> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Profil");
        dialog.setHeaderText("Créer un nouveau profil de joueur");

        // Appliquer le style
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/main.css").toExternalForm()
        );

        // Boutons
        ButtonType createButton = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButton, ButtonType.CANCEL);

        // Formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Prénom");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Nom");
        TextField nicknameField = new TextField();
        nicknameField.setPromptText("Pseudo (optionnel)");

        grid.add(new Label("Prénom:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Nom:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Pseudo:"), 0, 2);
        grid.add(nicknameField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Focus sur le premier champ
        Platform.runLater(firstNameField::requestFocus);

        // Validation en temps réel
        Button createBtn = (Button) dialog.getDialogPane().lookupButton(createButton);
        createBtn.setDisable(true);

        firstNameField.textProperty().addListener((obs, oldText, newText) -> {
            createBtn.setDisable(newText.trim().isEmpty() || lastNameField.getText().trim().isEmpty());
        });

        lastNameField.textProperty().addListener((obs, oldText, newText) -> {
            createBtn.setDisable(newText.trim().isEmpty() || firstNameField.getText().trim().isEmpty());
        });

        // Convertisseur de résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButton) {
                String firstName = firstNameField.getText().trim();
                String lastName = lastNameField.getText().trim();
                String nickname = nicknameField.getText().trim();

                if (!firstName.isEmpty() && !lastName.isEmpty()) {
                    return new PlayerProfile(firstName, lastName, nickname.isEmpty() ? firstName : nickname);
                }
            }
            return null;
        });

        // Afficher et traiter le résultat
        dialog.showAndWait().ifPresent(profile -> {
            profileManager.addProfile(profile);
            loadProfiles();
            setupComboBoxes();

            // Sélectionner le nouveau profil
            if (playerNumber == 1) {
                player1ComboBox.setValue(profile);
            } else {
                player2ComboBox.setValue(profile);
            }

            SoundManager.getInstance().playSound("powerup_collect");
        });
    }

    /**
     * Gère le clic sur "Commencer"
     */
    @FXML
    private void handleStart() {
        PlayerProfile profile1 = player1ComboBox.getValue();
        PlayerProfile profile2 = player2ComboBox.getValue();

        if (profile1 == null || profile2 == null) {
            showWarning("Sélection invalide", "Veuillez sélectionner deux profils.");
            return;
        }

        // Vérifier si les deux joueurs humains ont le même profil
        boolean p1IsBot = player1BotCheckbox != null && player1BotCheckbox.isSelected();
        boolean p2IsBot = player2BotCheckbox != null && player2BotCheckbox.isSelected();

        if (!p1IsBot && !p2IsBot && profile1.equals(profile2)) {
            showWarning("Sélection invalide", "Veuillez sélectionner deux profils différents pour les joueurs humains.");
            return;
        }

        SoundManager.getInstance().playSound("game_start");

        try {
            // Charger la vue du jeu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GameView.fxml"));
            Parent root = loader.load();

            // Obtenir le contrôleur et démarrer le jeu avec les paramètres de bot
            GameController gameController = loader.getController();
            gameController.startGame(profile1, profile2, p1IsBot, p2IsBot);

            // Créer la scène
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());

            // Changer de scène
            Stage stage = (Stage) startButton.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de démarrer le jeu: " + e.getMessage());
        }
    }

    /**
     * Gère le clic sur "Retour"
     */
    @FXML
    private void handleBack() {
        SoundManager.getInstance().playSound("menu_select");

        try {
            // Retour au menu principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainMenu.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());

            Stage stage = (Stage) startButton.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de retourner au menu: " + e.getMessage());
        }
    }

    /**
     * Affiche un avertissement
     */
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/main.css").toExternalForm()
        );
        alert.showAndWait();
    }

    /**
     * Affiche une erreur
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/main.css").toExternalForm()
        );
        alert.showAndWait();
    }

    /**
     * Convertisseur pour afficher les profils dans les ComboBox
     */
    private static class ProfileStringConverter extends StringConverter<PlayerProfile> {
        @Override
        public String toString(PlayerProfile profile) {
            if (profile == null) {
                return "";
            }
            return profile.getDisplayName() + " (" + profile.getGamesPlayed() + " parties)";
        }

        @Override
        public PlayerProfile fromString(String string) {
            return null; // Non utilisé
        }
    }
}