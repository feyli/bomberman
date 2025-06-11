package fr.amu.iut.bomberman.controller;

import fr.amu.iut.bomberman.utils.SceneManager;
import fr.amu.iut.bomberman.utils.SoundManager;
import fr.amu.iut.bomberman.utils.ThemeManager;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * Contrôleur du menu principal
 * Gère la navigation et les actions du menu
 *
 * @author Groupe_3_6
 * @version 1.0
 */
public class MainMenuController {

    @FXML
    private Button playButton;
    @FXML
    private Button profileButton;
    @FXML
    private Button quitButton;
    @FXML
    private ImageView backgroundImage; // Référence à l'image d'arrière-plan

    // Gestionnaire de thème
    private final ThemeManager themeManager = ThemeManager.getInstance();

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        System.out.println("Menu principal initialisé");

        // Configuration des boutons
        setupButtonEffects();

        // Activer le bouton profils
        if (profileButton != null) {
            profileButton.setDisable(false);
        }

        // Charger l'arrière-plan basé sur le thème actuel
        loadBackgroundImage();

        // Jouer la musique du menu
        SoundManager.getInstance().playMusic("menu_theme");
    }

    /**
     * Charge l'image d'arrière-plan en fonction du thème actuel
     */
    private void loadBackgroundImage() {
        if (backgroundImage != null) {
            try {
                String backgroundPath = themeManager.getBackgroundImagePath();
                System.out.println("Chargement de l'arrière-plan du menu: " + backgroundPath);

                // Charger l'image d'arrière-plan
                Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(backgroundPath)));
                backgroundImage.setImage(image);

                // Adapter l'image à la taille de la fenêtre
                backgroundImage.fitWidthProperty().bind(backgroundImage.getScene().getWindow().widthProperty());
                backgroundImage.fitHeightProperty().bind(backgroundImage.getScene().getWindow().heightProperty());
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de l'arrière-plan: " + e.getMessage());
            }
        }
    }

    /**
     * Configure les effets des boutons
     */
    private void setupButtonEffects() {
        Button[] buttons = {playButton, profileButton, quitButton};

        for (Button button : buttons) {
            if (button != null) {
                // Effet au survol
                button.setOnMouseEntered(e -> {
                    SoundManager.getInstance().playSound("menu_hover");
                    ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
                    st.setToX(1.1);
                    st.setToY(1.1);
                    st.play();
                });

                button.setOnMouseExited(e -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
                    st.setToX(1.0);
                    st.setToY(1.0);
                    st.play();
                });
            }
        }
    }

    /**
     * Action du bouton Jouer
     */
    @FXML
    private void handlePlay() {
        System.out.println("Bouton Jouer cliqué");
        SoundManager.getInstance().playSound("menu_select");

        try {
            // Charger la vue de sélection des joueurs
            URL fxmlUrl = getClass().getResource("/fxml/PlayerSelection.fxml");
            System.out.println("URL du FXML PlayerSelection: " + fxmlUrl);

            if (fxmlUrl == null) {
                showError("Erreur", "Le fichier PlayerSelection.fxml n'est pas trouvé dans /fxml/!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Obtenir la scène actuelle
            Stage stage = (Stage) playButton.getScene().getWindow();

            // Utiliser SceneManager pour changer de scène et préserver le mode plein écran et le thème actuel
            SceneManager.getInstance().changeScene(stage, root,
                    Objects.requireNonNull(getClass().getResource(ThemeManager.getInstance().getThemeCssPath())).toExternalForm());

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation",
                    "Impossible de charger la sélection des joueurs.\n" +
                            "Détails: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur inattendue",
                    "Une erreur inattendue s'est produite.\n" +
                            "Détails: " + e.getMessage());
        }
    }

    /**
     * Action du bouton Profils
     */
    @FXML
    private void handleProfiles() {
        System.out.println("Bouton Profils cliqué");
        SoundManager.getInstance().playSound("menu_select");

        try {
            // Charger la vue de gestion des profils
            URL fxmlUrl = getClass().getResource("/fxml/ProfileManager.fxml");
            System.out.println("URL du FXML ProfileManager: " + fxmlUrl);

            if (fxmlUrl == null) {
                showError("Erreur", "Le fichier ProfileManager.fxml n'est pas trouvé dans /fxml/!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Obtenir la scène actuelle
            Stage stage = (Stage) profileButton.getScene().getWindow();

            // Utiliser SceneManager pour changer de scène et préserver le mode plein écran et le thème actuel
            SceneManager.getInstance().changeScene(stage, root,
                    Objects.requireNonNull(getClass().getResource(ThemeManager.getInstance().getThemeCssPath())).toExternalForm());

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation",
                    "Impossible de charger la gestion des profils.\n" +
                            "Détails: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur inattendue",
                    "Une erreur inattendue s'est produite.\n" +
                            "Détails: " + e.getMessage());
        }
    }

    /**
     * Action du bouton Paramètres
     */
    @FXML
    private void handleSettings() {
        System.out.println("Bouton Paramètres cliqué");
        SoundManager.getInstance().playSound("menu_select");

        try {
            // Charger la vue des paramètres
            URL fxmlUrl = getClass().getResource("/fxml/Settings.fxml");
            System.out.println("URL du FXML Settings: " + fxmlUrl);

            if (fxmlUrl == null) {
                showError("Erreur", "Le fichier Settings.fxml n'est pas trouvé dans /fxml/!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Obtenir la scène actuelle
            Stage stage = (Stage) playButton.getScene().getWindow();

            // Utiliser SceneManager pour changer de scène et préserver le mode plein écran et le thème actuel
            SceneManager.getInstance().changeScene(stage, root,
                    Objects.requireNonNull(getClass().getResource(ThemeManager.getInstance().getThemeCssPath())).toExternalForm());

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation",
                    "Impossible de charger les paramètres.\n" +
                            "Détails: " + e.getMessage());
        }
    }

    /**
     * Action du bouton Quitter
     */
    @FXML
    private void handleQuit() {
        System.out.println("Bouton Quitter cliqué");
        SoundManager.getInstance().playSound("menu_select");

        // Confirmation de sortie
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Quitter");
        alert.setHeaderText("Voulez-vous vraiment quitter ?");
        alert.setContentText("Toute progression non sauvegardée sera perdue.");

        // Appliquer le thème actuel au dialogue
        alert.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource(ThemeManager.getInstance().getThemeCssPath())).toExternalForm()
        );

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Arrêter la musique avant de quitter
                SoundManager.getInstance().stopMusic();
                Platform.exit();
            }
        });
    }

    /**
     * Affiche une erreur
     *
     * @param title   Titre de l'erreur
     * @param message Message d'erreur
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Appliquer le thème actuel au dialogue
        alert.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource(ThemeManager.getInstance().getThemeCssPath())).toExternalForm()
        );

        alert.showAndWait();
    }
}