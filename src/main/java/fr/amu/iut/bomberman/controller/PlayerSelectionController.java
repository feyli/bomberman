package fr.amu.iut.bomberman.controller;

import fr.amu.iut.bomberman.model.PlayerProfile;
import fr.amu.iut.bomberman.utils.FullScreenManager;
import fr.amu.iut.bomberman.utils.ProfileManager;
import fr.amu.iut.bomberman.utils.SceneManager;
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
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * Contrôleur pour la sélection des joueurs
 * Permet de choisir les profils avant de commencer une partie
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

    // Champs pour le mode bot
    @FXML
    private CheckBox botModeCheckbox;
    @FXML
    private ComboBox<String> botDifficultyCombo;
    @FXML
    private Label botDifficultyLabel;

    private boolean botModeEnabled = false;
    private final PlayerProfile botProfile = new PlayerProfile("Bot", "Bomberman", "BOT");

    private ProfileManager profileManager;
    private ObservableList<PlayerProfile> profiles;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        System.out.println("PlayerSelectionController initialisé");

        profileManager = ProfileManager.getInstance();
        loadProfiles();

        // Configuration des ComboBox
        setupComboBoxes();

        // Configuration de la ComboBox de temps
        setupTimeComboBox();

        // Configuration de la difficulté du bot
        setupBotDifficultyCombo();

        // Listeners pour activer/désactiver le bouton Start
        player1ComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updatePlayer1Display(newVal);
            checkStartButtonState();
        });

        player2ComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updatePlayer2Display(newVal);
            checkStartButtonState();
        });

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
        if (!profiles.isEmpty()) {
            player1ComboBox.setValue(profiles.get(0));
        }
        if (profiles.size() > 1) {
            player2ComboBox.setValue(profiles.get(1));
        } else if (!profiles.isEmpty()) {
            // S'il n'y a qu'un profil, on sélectionne le même pour les deux (sera corrigé plus tard)
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
     * Configure la ComboBox de difficulté du bot
     */
    private void setupBotDifficultyCombo() {
        if (botDifficultyCombo != null) {
            botDifficultyCombo.setItems(FXCollections.observableArrayList(
                    "Facile", "Normal", "Difficile"
            ));
            botDifficultyCombo.setValue("Normal");
        }
    }

    /**
     * Gère l'activation/désactivation du mode bot
     */
    @FXML
    private void handleBotModeToggle() {
        botModeEnabled = botModeCheckbox.isSelected();

        // Afficher/masquer les options de difficulté du bot
        botDifficultyLabel.setVisible(botModeEnabled);
        botDifficultyCombo.setVisible(botModeEnabled);

        // Si mode bot activé, configurer automatiquement le joueur 2 comme bot
        if (botModeEnabled) {
            player2ComboBox.setDisable(true);
            updatePlayer2Display(botProfile);
        } else {
            player2ComboBox.setDisable(false);
            updatePlayer2Display(player2ComboBox.getValue());
        }

        // Vérifier l'état du bouton Start
        checkStartButtonState();

        // Jouer un son
        SoundManager.getInstance().playSound("menu_hover");
    }

    /**
     * Met à jour l'affichage du joueur 1
     *
     * @param profile Profil sélectionné
     */
    private void updatePlayer1Display(PlayerProfile profile) {
        if (profile != null && player1Avatar != null) {
            try {
                Image avatar = new Image(Objects.requireNonNull(getClass().getResourceAsStream(profile.getAvatarPath())));
                player1Avatar.setImage(avatar);
            } catch (Exception e) {
                // Avatar par défaut en cas d'erreur
                System.err.println("Impossible de charger l'avatar: " + profile.getAvatarPath());
                try {
                    Image defaultAvatar = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/avatars/default.png")));
                    player1Avatar.setImage(defaultAvatar);
                } catch (Exception ex) {
                    System.err.println("Impossible de charger l'avatar par défaut");
                }
            }
        }
    }

    /**
     * Met à jour l'affichage du joueur 2
     *
     * @param profile Profil sélectionné
     */
    private void updatePlayer2Display(PlayerProfile profile) {
        if (profile != null && player2Avatar != null) {
            try {
                Image avatar = new Image(Objects.requireNonNull(getClass().getResourceAsStream(profile.getAvatarPath())));
                player2Avatar.setImage(avatar);
            } catch (Exception e) {
                // Avatar par défaut en cas d'erreur
                System.err.println("Impossible de charger l'avatar: " + profile.getAvatarPath());
                try {
                    Image defaultAvatar = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/avatars/default.png")));
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

        // Pour le mode bot, seul le profil du joueur 1 est nécessaire
        if (botModeEnabled) {
            boolean canStart = p1 != null;
            startButton.setDisable(!canStart);
            return;
        }

        // Pour le mode normal, il faut deux profils différents
        PlayerProfile p2 = player2ComboBox.getValue();
        boolean canStart = p1 != null && p2 != null && !p1.equals(p2);
        startButton.setDisable(!canStart);

        if (p1 != null && p1.equals(p2)) {
            // Ne pas afficher l'avertissement immédiatement, juste désactiver le bouton
            System.out.println("Même profil sélectionné pour les deux joueurs");
        }
    }

    /**
     * Gère le clic sur "Nouveau Profil" pour le joueur 1
     */
    @FXML
    private void handleNewProfile1() {
        SoundManager.getInstance().playSound("menu_select");
        createNewProfile(1);
    }

    /**
     * Gère le clic sur "Nouveau Profil" pour le joueur 2
     */
    @FXML
    private void handleNewProfile2() {
        SoundManager.getInstance().playSound("menu_select");
        createNewProfile(2);
    }

    /**
     * Crée un nouveau profil
     *
     * @param playerNumber Numéro du joueur
     */
    private void createNewProfile(int playerNumber) {
        // Dialogue de création de profil
        Dialog<PlayerProfile> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Profil");
        dialog.setHeaderText("Créer un nouveau profil de joueur");

        // Appliquer le style
        dialog.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/main.css")).toExternalForm()
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
    private void handleStartGame() {
        // Vérifier les sélections de profils
        if (player1ComboBox.getValue() == null) {
            showWarning("Veuillez sélectionner un profil pour le joueur 1.");
            return;
        }

        // En mode normal, vérifier le profil du joueur 2
        if (!botModeEnabled && player2ComboBox.getValue() == null) {
            showWarning("Veuillez sélectionner un profil pour le joueur 2.");
            return;
        }

        // Récupérer les données de jeu
        PlayerProfile profile1 = player1ComboBox.getValue();
        PlayerProfile profile2 = botModeEnabled ? botProfile : player2ComboBox.getValue();
        int rounds = roundsSpinner.getValue();
        String timeString = timeComboBox.getValue();

        // Convertir le temps sélectionné en secondes
        int timeInSeconds = convertTimeToSeconds(timeString);

        SoundManager.getInstance().playSound("game_start");

        try {
            // Charger la vue du jeu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GameView.fxml"));
            Parent root = loader.load();

            // Obtenir le contrôleur et démarrer le jeu
            GameController gameController = loader.getController();

            // Si mode bot activé, utiliser la difficulté sélectionnée
            if (botModeEnabled) {
                String difficulty = botDifficultyCombo.getValue();
                gameController.startGameWithBot(profile1, difficulty, rounds, timeInSeconds);
            } else {
                gameController.startGame(profile1, profile2, rounds, timeInSeconds);
            }

            // Créer la scène
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/main.css")).toExternalForm());

            // Changer de scène
            Stage stage = (Stage) startButton.getScene().getWindow();
            stage.setScene(scene);

            // Configurer le plein écran pour le jeu
            FullScreenManager.getInstance().configureForGame(stage);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossible de démarrer le jeu: " + e.getMessage());
        }
    }

    /**
     * Convertit le temps affiché (MM:SS) en secondes
     *
     * @param timeString Temps au format MM:SS
     * @return Temps en secondes
     */
    private int convertTimeToSeconds(String timeString) {
        String[] parts = timeString.split(":");
        if (parts.length == 2) {
            try {
                int minutes = Integer.parseInt(parts[0]);
                int seconds = Integer.parseInt(parts[1]);
                return minutes * 60 + seconds;
            } catch (NumberFormatException e) {
                System.err.println("Format de temps invalide: " + timeString);
            }
        }
        // Valeur par défaut en cas d'erreur (3 minutes)
        return 180;
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
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/main.css")).toExternalForm());

            Stage stage = (Stage) startButton.getScene().getWindow();
            stage.setScene(scene);

            // Configurer pour le mode menu (sans plein écran)
            FullScreenManager.getInstance().configureForMenu(stage);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation: " + e.getMessage());
        }
    }

    /**
     * Affiche une erreur
     *
     * @param message Message d'erreur
     */
    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Erreur", message);
    }

    /**
     * Affiche un avertissement
     *
     * @param message Message d'avertissement
     */
    private void showWarning(String message) {
        showAlert(Alert.AlertType.WARNING, "Attention", message);
    }

    /**
     * Affiche une alerte
     *
     * @param type    Type d'alerte
     * @param title   Titre de l'alerte
     * @param message Message de l'alerte
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Appliquer le style
        alert.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/main.css")).toExternalForm()
        );

        alert.showAndWait();
    }

    /**
     * Classe utilitaire pour convertir un ProfilePlayer en affichage String
     */
    private static class ProfileStringConverter extends StringConverter<PlayerProfile> {
        @Override
        public String toString(PlayerProfile profile) {
            if (profile == null) return "";
            return profile.getNickname();
        }

        @Override
        public PlayerProfile fromString(String string) {
            return null; // Non utilisé car nous ne modifions pas les profils ici
        }
    }
}

