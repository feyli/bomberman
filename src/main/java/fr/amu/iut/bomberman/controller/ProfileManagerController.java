package fr.amu.iut.bomberman.controller;

import fr.amu.iut.bomberman.model.PlayerProfile;
import fr.amu.iut.bomberman.utils.ProfileManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Contrôleur pour la gestion des profils de joueurs
 * Permet de créer, modifier et supprimer des profils
 *
 * @author Super Bomberman Team
 * @version 1.0
 */
public class ProfileManagerController {

    @FXML
    private ListView<PlayerProfile> profileListView;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    @FXML
    private ImageView avatarImageView;
    @FXML
    private Label firstNameLabel;
    @FXML
    private Label lastNameLabel;
    @FXML
    private Label nicknameLabel;
    @FXML
    private Label gamesPlayedLabel;
    @FXML
    private Label gamesWonLabel;
    @FXML
    private Label winRateLabel;
    @FXML
    private Label totalScoreLabel;
    @FXML
    private Label createdDateLabel;

    private ProfileManager profileManager;
    private PlayerProfile selectedProfile;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        profileManager = ProfileManager.getInstance();

        // Configuration de la ListView
        setupListView();

        // Charger les profils
        refreshProfileList();

        // Désactiver les boutons au départ
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    /**
     * Configure la ListView des profils
     */
    private void setupListView() {
        // Affichage personnalisé des profils
        profileListView.setCellFactory(listView -> new ListCell<PlayerProfile>() {
            @Override
            protected void updateItem(PlayerProfile profile, boolean empty) {
                super.updateItem(profile, empty);
                if (empty || profile == null) {
                    setText(null);
                } else {
                    setText(String.format("%s - %d parties (%.1f%% victoires)",
                            profile.getDisplayName(),
                            profile.getGamesPlayed(),
                            profile.getWinRate()));
                }
            }
        });

        // Listener pour la sélection
        profileListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    selectedProfile = newSelection;
                    updateProfileDetails(newSelection);
                    updateButtonStates();
                }
        );
    }

    /**
     * Rafraîchit la liste des profils
     */
    private void refreshProfileList() {
        profileListView.getItems().clear();
        profileListView.getItems().addAll(profileManager.getAllProfiles());
    }

    /**
     * Met à jour l'affichage des détails du profil
     *
     * @param profile Profil sélectionné
     */
    private void updateProfileDetails(PlayerProfile profile) {
        if (profile == null) {
            // Effacer les détails
            avatarImageView.setImage(null);
            firstNameLabel.setText("-");
            lastNameLabel.setText("-");
            nicknameLabel.setText("-");
            gamesPlayedLabel.setText("-");
            gamesWonLabel.setText("-");
            winRateLabel.setText("-");
            totalScoreLabel.setText("-");
            createdDateLabel.setText("-");
        } else {
            // Afficher les détails
            try {
                Image avatar = new Image(getClass().getResourceAsStream(profile.getAvatarPath()));
                avatarImageView.setImage(avatar);
            } catch (Exception e) {
                // Avatar par défaut
            }

            firstNameLabel.setText(profile.getFirstName());
            lastNameLabel.setText(profile.getLastName());
            nicknameLabel.setText(profile.getNickname());
            gamesPlayedLabel.setText(String.valueOf(profile.getGamesPlayed()));
            gamesWonLabel.setText(String.valueOf(profile.getGamesWon()));
            winRateLabel.setText(String.format("%.1f%%", profile.getWinRate()));
            totalScoreLabel.setText(String.valueOf(profile.getTotalScore()));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            createdDateLabel.setText(profile.getCreatedDate().format(formatter));
        }
    }

    /**
     * Met à jour l'état des boutons
     */
    private void updateButtonStates() {
        boolean hasSelection = selectedProfile != null;
        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
    }

    /**
     * Gère la création d'un nouveau profil
     */
    @FXML
    private void handleNewProfile() {
        Dialog<PlayerProfile> dialog = createProfileDialog("Nouveau Profil", null);

        dialog.showAndWait().ifPresent(profile -> {
            profileManager.addProfile(profile);
            refreshProfileList();
            profileListView.getSelectionModel().select(profile);
        });
    }

    /**
     * Gère la modification d'un profil
     */
    @FXML
    private void handleEditProfile() {
        if (selectedProfile == null) return;

        Dialog<PlayerProfile> dialog = createProfileDialog("Modifier le Profil", selectedProfile);

        dialog.showAndWait().ifPresent(updatedProfile -> {
            // Mettre à jour les propriétés
            selectedProfile.setFirstName(updatedProfile.getFirstName());
            selectedProfile.setLastName(updatedProfile.getLastName());
            selectedProfile.setNickname(updatedProfile.getNickname());

            profileManager.updateProfile(selectedProfile);
            refreshProfileList();
            updateProfileDetails(selectedProfile);
        });
    }

    /**
     * Gère la suppression d'un profil
     */
    @FXML
    private void handleDeleteProfile() {
        if (selectedProfile == null) return;

        // Confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer le profil");
        alert.setHeaderText("Voulez-vous vraiment supprimer ce profil ?");
        alert.setContentText("Profil: " + selectedProfile.getDisplayName() +
                "\nCette action est irréversible.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                profileManager.removeProfile(selectedProfile);
                refreshProfileList();
                selectedProfile = null;
                updateProfileDetails(null);
            }
        });
    }

    /**
     * Crée un dialogue pour créer/modifier un profil
     *
     * @param title   Titre du dialogue
     * @param profile Profil à modifier (null pour création)
     * @return Dialogue configuré
     */
    private Dialog<PlayerProfile> createProfileDialog(String title, PlayerProfile profile) {
        Dialog<PlayerProfile> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(profile == null ? "Créer un nouveau profil" : "Modifier le profil");

        // Boutons
        ButtonType saveButton = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        // Formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField firstNameField = new TextField(profile != null ? profile.getFirstName() : "");
        TextField lastNameField = new TextField(profile != null ? profile.getLastName() : "");
        TextField nicknameField = new TextField(profile != null ? profile.getNickname() : "");

        grid.add(new Label("Prénom:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Nom:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Pseudo:"), 0, 2);
        grid.add(nicknameField, 1, 2);

        // TODO: Ajouter la sélection d'avatar

        dialog.getDialogPane().setContent(grid);

        // Validation
        dialog.getDialogPane().lookupButton(saveButton).setDisable(true);

        firstNameField.textProperty().addListener((obs, oldText, newText) -> {
            dialog.getDialogPane().lookupButton(saveButton).setDisable(
                    newText.trim().isEmpty() || lastNameField.getText().trim().isEmpty()
            );
        });

        lastNameField.textProperty().addListener((obs, oldText, newText) -> {
            dialog.getDialogPane().lookupButton(saveButton).setDisable(
                    newText.trim().isEmpty() || firstNameField.getText().trim().isEmpty()
            );
        });

        // Convertisseur de résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                return new PlayerProfile(
                        firstNameField.getText().trim(),
                        lastNameField.getText().trim(),
                        nicknameField.getText().trim()
                );
            }
            return null;
        });

        return dialog;
    }

    /**
     * Exporte les statistiques en CSV
     */
    @FXML
    private void handleExportStats() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les statistiques");
        fileChooser.setInitialFileName("bomberman_stats.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );

        File file = fileChooser.showSaveDialog(profileListView.getScene().getWindow());
        if (file != null) {
            if (profileManager.exportStatistics(file.getName())) {
                showInfo("Export réussi", "Les statistiques ont été exportées avec succès.");
            } else {
                showError("Erreur d'export", "Impossible d'exporter les statistiques.");
            }
        }
    }

    /**
     * Retour au menu principal
     */
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainMenu.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());

            Stage stage = (Stage) profileListView.getScene().getWindow();
            stage.setScene(scene);

        } catch (IOException e) {
            showError("Erreur", "Impossible de retourner au menu: " + e.getMessage());
        }
    }

    /**
     * Affiche une information
     *
     * @param title   Titre
     * @param message Message
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une erreur
     *
     * @param title   Titre
     * @param message Message
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}