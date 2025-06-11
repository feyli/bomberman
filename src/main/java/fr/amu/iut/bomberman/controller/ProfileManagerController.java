package fr.amu.iut.bomberman.controller;

import fr.amu.iut.bomberman.model.PlayerProfile;
import fr.amu.iut.bomberman.utils.ProfileManager;
import fr.amu.iut.bomberman.utils.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);

        // Formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 20, 20));

        TextField firstNameField = new TextField(profile != null ? profile.getFirstName() : "");
        TextField lastNameField = new TextField(profile != null ? profile.getLastName() : "");
        TextField nicknameField = new TextField(profile != null ? profile.getNickname() : "");

        grid.add(new Label("Prénom:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Nom:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Pseudo:"), 0, 2);
        grid.add(nicknameField, 1, 2);

        // Sélection d'avatar
        grid.add(new Label("Avatar:"), 0, 3);

        // Zone pour les avatars disponibles
        HBox avatarContainer = new HBox(10);
        avatarContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Avatar sélectionné actuellement
        ImageView selectedAvatarView = new ImageView();
        selectedAvatarView.setFitWidth(64);
        selectedAvatarView.setFitHeight(64);

        // Chemin de l'avatar actuellement sélectionné
        String[] selectedAvatarPath = new String[1];
        selectedAvatarPath[0] = profile != null ? profile.getAvatarPath() : "/images/avatars/default.png";

        try {
            selectedAvatarView.setImage(new Image(getClass().getResourceAsStream(selectedAvatarPath[0])));
        } catch (Exception e) {
            selectedAvatarView.setImage(new Image(getClass().getResourceAsStream("/images/avatars/default.png")));
        }

        avatarContainer.getChildren().add(selectedAvatarView);

        // Bouton pour choisir l'avatar
        Button chooseAvatarButton = new Button("Choisir");
        avatarContainer.getChildren().add(chooseAvatarButton);

        grid.add(avatarContainer, 1, 3);

        // Conteneur principal
        VBox mainContainer = new VBox(20);
        mainContainer.getChildren().add(grid);

        // Personnaliser la position des boutons
        ButtonBar buttonBar = (ButtonBar) dialog.getDialogPane().lookup(".button-bar");
        buttonBar.getButtons().clear(); // Supprimer les boutons par défaut

        // Créer nos propres boutons
        Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(cancelButton);
        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveButton);

        // Définir la taille des boutons
        cancelBtn.setPrefWidth(100);
        saveBtn.setPrefWidth(100);

        // Conteneur pour les boutons
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);
        buttonContainer.getChildren().addAll(cancelBtn, saveBtn);

        // Ajouter les boutons au conteneur principal
        mainContainer.getChildren().add(buttonContainer);

        dialog.getDialogPane().setContent(mainContainer);

        // Gestionnaire pour le choix d'avatar
        chooseAvatarButton.setOnAction(e -> {
            Dialog<String> avatarDialog = new Dialog<>();
            avatarDialog.setTitle("Choisir un avatar");
            avatarDialog.setHeaderText("Sélectionnez votre avatar");

            // Grille d'avatars
            GridPane avatarGrid = new GridPane();
            avatarGrid.setHgap(10);
            avatarGrid.setVgap(10);
            avatarGrid.setPadding(new javafx.geometry.Insets(20, 20, 20, 20));

            // Liste des avatars disponibles
            String[] avatarFiles = {
                    "/images/avatars/default.png",
                    "/images/avatars/avatar1.png",
                    "/images/avatars/avatar2.png",
                    "/images/avatars/avatar3.png",
                    "/images/avatars/avatar4.png",
                    "/images/avatars/avatar5.png",
                    "/images/avatars/avatar6.png",
                    "/images/avatars/avatar7.png",
                    "/images/avatars/avatar8.png"
            };

            ToggleGroup avatarToggleGroup = new ToggleGroup();
            int col = 0;
            int row = 0;

            for (String avatarFile : avatarFiles) {
                try {
                    Image avatarImage = new Image(getClass().getResourceAsStream(avatarFile));
                    ImageView avatarView = new ImageView(avatarImage);
                    avatarView.setFitWidth(64);
                    avatarView.setFitHeight(64);

                    RadioButton avatarRadio = new RadioButton();
                    avatarRadio.setGraphic(avatarView);
                    avatarRadio.setToggleGroup(avatarToggleGroup);
                    avatarRadio.setUserData(avatarFile);

                    // Sélectionner l'avatar actuel
                    if (avatarFile.equals(selectedAvatarPath[0])) {
                        avatarRadio.setSelected(true);
                    }

                    avatarGrid.add(avatarRadio, col, row);

                    col++;
                    if (col > 2) {
                        col = 0;
                        row++;
                    }
                } catch (Exception ex) {
                    System.err.println("Erreur lors du chargement de l'avatar: " + avatarFile);
                }
            }

            // Boutons du dialogue
            ButtonType selectButton = new ButtonType("Sélectionner", ButtonBar.ButtonData.OK_DONE);
            avatarDialog.getDialogPane().getButtonTypes().addAll(selectButton, ButtonType.CANCEL);

            avatarDialog.getDialogPane().setContent(avatarGrid);

            // Résultat du dialogue
            avatarDialog.setResultConverter(dialogBtn -> {
                if (dialogBtn == selectButton && avatarToggleGroup.getSelectedToggle() != null) {
                    return (String) avatarToggleGroup.getSelectedToggle().getUserData();
                }
                return null;
            });

            // Traiter le résultat
            avatarDialog.showAndWait().ifPresent(avatarPath -> {
                selectedAvatarPath[0] = avatarPath;
                selectedAvatarView.setImage(new Image(getClass().getResourceAsStream(avatarPath)));
            });
        });

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
                PlayerProfile newProfile = new PlayerProfile(
                        firstNameField.getText().trim(),
                        lastNameField.getText().trim(),
                        nicknameField.getText().trim()
                );
                newProfile.setAvatarPath(selectedAvatarPath[0]);
                return newProfile;
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
            if (profileManager.exportStatistics(file.getAbsolutePath())) {
                showInfo("Export réussi", "Les statistiques ont été exportées avec succès.");
            } else {
                showError("Erreur d'export", "Impossible d'exporter les statistiques.");
            }
        }
    }

    /**
     * Importe les statistiques depuis un fichier CSV
     */
    @FXML
    private void handleImportStats() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer les statistiques");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );

        File file = fileChooser.showOpenDialog(profileListView.getScene().getWindow());
        if (file != null) {
            if (profileManager.importStatistics(file.getAbsolutePath())) {
                refreshProfileList(); // Actualiser la liste des profils
                showInfo("Import réussi", "Les statistiques ont été importées avec succès.");
            } else {
                showError("Erreur d'import", "Impossible d'importer les statistiques. Vérifiez le format du fichier.");
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
            scene.getStylesheets().add(getClass().getResource(ThemeManager.getInstance().getThemeCssPath()).toExternalForm());

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
